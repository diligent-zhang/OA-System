package com.tianyu.oasystem.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.AttRuleDTO;
import com.tianyu.oasystem.dto.PunchDTO;
import com.tianyu.oasystem.entity.AttRecord;
import com.tianyu.oasystem.entity.AttRule;
import com.tianyu.oasystem.entity.FlowInstance;
import com.tianyu.oasystem.mapper.AttRecordMapper;
import com.tianyu.oasystem.mapper.AttRuleMapper;
import com.tianyu.oasystem.mapper.EmpEmployeeMapper;
import com.tianyu.oasystem.service.AttendanceService;
import com.tianyu.oasystem.util.UserContext;
import com.tianyu.oasystem.vo.AttReportRowVO;
import com.tianyu.oasystem.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HH:mm");
    private static final Pattern OVERTIME_HOURS = Pattern.compile("时长\\s*([\\d.]+)\\s*小时");

    private final AttRuleMapper ruleMapper;
    private final AttRecordMapper recordMapper;
    private final EmpEmployeeMapper employeeMapper;
    private final ObjectMapper objectMapper;

    // ==================== 规则 ====================

    @Override
    public List<AttRule> listRules() {
        return ruleMapper.selectAll();
    }

    @Override
    public AttRule activeRule() {
        return ruleMapper.selectActive();
    }

    @Override
    public AttRule saveRule(AttRuleDTO dto) {
        AttRule rule = new AttRule();
        apply(rule, dto);
        rule.setEnabled(1);
        // 支持多规则并存，但保持单一生效规则：保存新规则时关闭旧规则
        AttRule active = ruleMapper.selectActive();
        ruleMapper.insert(rule);
        if (active != null) {
            ruleMapper.updateEnabled(active.getRuleId(), 0);
        }
        return rule;
    }

    @Override
    public void updateRule(Long ruleId, AttRuleDTO dto) {
        requireRule(ruleId);
        AttRule rule = new AttRule();
        rule.setRuleId(ruleId);
        apply(rule, dto);
        ruleMapper.update(rule);
    }

    @Override
    public void deleteRule(Long ruleId) {
        requireRule(ruleId);
        ruleMapper.deleteById(ruleId);
    }

    @Override
    public void toggleRule(Long ruleId, Integer enabled) {
        requireRule(ruleId);
        ruleMapper.updateEnabled(ruleId, enabled == 1 ? 1 : 0);
    }

    private void apply(AttRule rule, AttRuleDTO dto) {
        rule.setRuleName(dto.getRuleName());
        rule.setWorkStart(dto.getWorkStart());
        rule.setWorkEnd(dto.getWorkEnd());
        rule.setLatitude(dto.getLatitude());
        rule.setLongitude(dto.getLongitude());
        rule.setRadiusM(dto.getRadiusM() == null ? 200 : dto.getRadiusM());
        rule.setLocationCheck(dto.getLocationCheck() == null ? 0 : dto.getLocationCheck());
        rule.setRestDays(writeRestDays(dto.getRestDays()));
    }

    // ==================== 打卡 ====================

    @Override
    public String punch(PunchDTO dto) {
        AttRule rule = ruleMapper.selectActive();
        if (rule == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "管理员尚未配置考勤规则");
        }
        Long empId = UserContext.get().getEmpId();
        if (empId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前账号未关联员工，无法打卡");
        }
        LocalDate today = LocalDate.now();
        if (isRestDay(rule, today)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "今日为休息日，无需打卡");
        }
        if (rule.getLocationCheck() != null && rule.getLocationCheck() == 1) {
            if (dto.getLatitude() == null || dto.getLongitude() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "定位不可用，无法打卡");
            }
            double dist = distance(rule.getLatitude(), rule.getLongitude(), dto.getLatitude(), dto.getLongitude());
            if (dist > rule.getRadiusM()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "不在考勤范围内，无法打卡");
            }
        }
        LocalTime workStart = LocalTime.parse(rule.getWorkStart(), HMS);
        LocalTime workEnd = LocalTime.parse(rule.getWorkEnd(), HMS);
        LocalDateTime now = LocalDateTime.now();
        AttRecord rec = recordMapper.selectByEmpAndDate(empId, today);

        if ("IN".equals(dto.getType())) {
            if (rec == null) {
                rec = new AttRecord();
                rec.setEmpId(empId);
                rec.setRecordDate(today);
                rec.setClockIn(now);
                rec.setStatus(now.toLocalTime().isAfter(workStart) ? "LATE" : "NORMAL");
                rec.setSource("MANUAL");
                recordMapper.insert(rec);
            } else if (rec.getClockIn() == null) {
                recordMapper.updateClockIn(rec.getId(), now,
                        now.toLocalTime().isAfter(workStart) ? "LATE" : "NORMAL");
            } else {
                throw new BusinessException(ResultCode.PARAM_ERROR, "今日已打过上班卡");
            }
            return "上班打卡成功";
        }

        // OUT
        if (rec == null) {
            rec = new AttRecord();
            rec.setEmpId(empId);
            rec.setRecordDate(today);
            rec.setClockOut(now);
            rec.setStatus("MISSING_IN");
            rec.setSource("MANUAL");
            recordMapper.insert(rec);
        } else if (rec.getClockOut() == null) {
            recordMapper.updateClockOut(rec.getId(), now, combinedStatus(rec.getClockIn(), now, workStart, workEnd));
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR, "今日已打过下班卡");
        }
        return "下班打卡成功";
    }

    /** 综合判定：迟到/早退/缺卡一次判定 */
    private String combinedStatus(LocalDateTime in, LocalDateTime out, LocalTime start, LocalTime end) {
        if (in == null) {
            return "MISSING_IN";
        }
        if (out == null) {
            return "MISSING_OUT";
        }
        boolean late = in.toLocalTime().isAfter(start);
        boolean early = out.toLocalTime().isBefore(end);
        if (late) {
            return "LATE";
        }
        return early ? "EARLY_LEAVE" : "NORMAL";
    }

    // ==================== 记录与报表 ====================

    @Override
    public List<AttRecord> myMonthly(String month) {
        Long empId = UserContext.get().getEmpId();
        YearMonth ym = parseMonth(month);
        return recordMapper.selectByEmpMonth(empId, ym.atDay(1), ym.atEndOfMonth());
    }

    @Override
    public List<AttReportRowVO> monthlyReport(String month, List<Long> deptIds) {
        AttRule rule = ruleMapper.selectActive();
        Set<Integer> restDays = restDayNumbers(rule);
        YearMonth ym = parseMonth(month);
        int shouldDays = 0;
        for (LocalDate d = ym.atDay(1); !d.isAfter(ym.atEndOfMonth()); d = d.plusDays(1)) {
            if (!restDays.contains(d.getDayOfWeek().getValue())) {
                shouldDays++;
            }
        }

        List<EmployeeVO> employees = employeeMapper.selectActiveList(deptIds);
        List<AttRecord> records = recordMapper.selectMonth(ym.atDay(1), ym.atEndOfMonth(), deptIds);
        Map<Long, List<AttRecord>> byEmp = new HashMap<>();
        for (AttRecord r : records) {
            byEmp.computeIfAbsent(r.getEmpId(), k -> new ArrayList<>()).add(r);
        }

        List<AttReportRowVO> rows = new ArrayList<>();
        for (EmployeeVO emp : employees) {
            AttReportRowVO row = new AttReportRowVO();
            row.setEmpId(emp.getEmpId());
            row.setEmpNo(emp.getEmpNo());
            row.setName(emp.getName());
            row.setDeptName(emp.getDeptName());
            row.setShouldDays(shouldDays);

            Set<LocalDate> presentDates = new HashSet<>();
            for (AttRecord r : byEmp.getOrDefault(emp.getEmpId(), new ArrayList<>())) {
                String st = r.getStatus();
                if ("LEAVE".equals(st)) {
                    row.setLeaveDays(row.getLeaveDays() + 1);
                } else if ("OVERTIME".equals(st)) {
                    row.setOvertimeCount(row.getOvertimeCount() + 1);
                    row.setOvertimeHours(row.getOvertimeHours() + parseOvertimeHours(r.getRemark()));
                } else {
                    if (!restDays.contains(r.getRecordDate().getDayOfWeek().getValue())) {
                        presentDates.add(r.getRecordDate());
                    }
                    if ("LATE".equals(st)) {
                        row.setLateCount(row.getLateCount() + 1);
                    } else if ("EARLY_LEAVE".equals(st)) {
                        row.setEarlyCount(row.getEarlyCount() + 1);
                    }
                    if (r.getClockIn() == null || r.getClockOut() == null) {
                        row.setMissingCount(row.getMissingCount() + 1);
                    }
                }
            }
            row.setActualDays(presentDates.size());
            int absent = shouldDays - row.getActualDays() - row.getLeaveDays();
            row.setAbsentDays(Math.max(absent, 0));
            rows.add(row);
        }
        return rows;
    }

    @Override
    public String exportCsv(String month, List<Long> deptIds) {
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("工号,姓名,部门,应出勤,实际出勤,迟到,早退,缺卡,请假天数,加班次数,加班小时,旷工\n");
        for (AttReportRowVO r : monthlyReport(month, deptIds)) {
            sb.append(csv(r.getEmpNo())).append(',')
                    .append(csv(r.getName())).append(',')
                    .append(csv(r.getDeptName())).append(',')
                    .append(r.getShouldDays()).append(',')
                    .append(r.getActualDays()).append(',')
                    .append(r.getLateCount()).append(',')
                    .append(r.getEarlyCount()).append(',')
                    .append(r.getMissingCount()).append(',')
                    .append(r.getLeaveDays()).append(',')
                    .append(r.getOvertimeCount()).append(',')
                    .append(r.getOvertimeHours()).append(',')
                    .append(r.getAbsentDays()).append('\n');
        }
        return sb.toString();
    }

    private String csv(String s) {
        if (s == null) {
            return "";
        }
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    // ==================== 审批联动 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onApproved(FlowInstance instance, Map<String, Object> data) {
        if (instance.getApplicantEmpId() == null) {
            return;
        }
        Long empId = instance.getApplicantEmpId();
        AttRule rule = ruleMapper.selectActive();
        Set<Integer> restDays = restDayNumbers(rule);

        if ("leave".equals(instance.getCategory())) {
            LocalDate begin = parseDate(data.get("begin_date"));
            LocalDate end = parseDate(data.get("end_date"));
            if (begin != null && end != null) {
                for (LocalDate d = begin; !d.isAfter(end); d = d.plusDays(1)) {
                    if (restDays.contains(d.getDayOfWeek().getValue())) {
                        continue;
                    }
                    upsert(empId, d, "LEAVE", "请假审批通过");// 联动生成请假考勤
                }
            }
        } else if ("overtime".equals(instance.getCategory())) {
            LocalDate date = parseDate(data.get("work_date"));
            if (date != null) {
                Object hours = data.get("hours");
                String remark = "加班审批通过，时长 " + (hours == null ? "" : hours) + " 小时";
                upsert(empId, date, "OVERTIME", remark);
            }
        }
    }

    private void upsert(Long empId, LocalDate date, String status, String remark) {
        AttRecord rec = recordMapper.selectByEmpAndDate(empId, date);
        if (rec == null) {
            rec = new AttRecord();
            rec.setEmpId(empId);
            rec.setRecordDate(date);
            rec.setStatus(status);
            rec.setSource("FLOW_LINK");
            rec.setRemark(remark);
            recordMapper.insert(rec);
        } else {
            recordMapper.updateStatus(rec.getId(), status, remark);
        }
    }

    // ==================== 工具 ====================

    private boolean isRestDay(AttRule rule, LocalDate day) {
        return restDayNumbers(rule).contains(day.getDayOfWeek().getValue());
    }

    /** 返回休息日集合（DayOfWeek.getValue，周一=1…周日=7）；配置态 0=周日 6=周六 */
    private Set<Integer> restDayNumbers(AttRule rule) {
        Set<Integer> set = new HashSet<>();
        if (rule == null || !StringUtils.hasText(rule.getRestDays())) {
            set.add(6);
            set.add(7);
            return set;
        }
        try {
            List<Integer> list = objectMapper.readValue(rule.getRestDays(), new TypeReference<>() {
            });
            for (Integer v : list) {
                set.add(v == 0 ? 7 : v);
            }
        } catch (Exception e) {
            set.add(6);
            set.add(7);
        }
        return set;
    }

    private String writeRestDays(List<String> restDays) {
        if (restDays == null || restDays.isEmpty()) {
            return "[\"6\",\"7\"]";
        }
        try {
            return objectMapper.writeValueAsString(restDays);
        } catch (Exception e) {
            return "[\"6\",\"7\"]";
        }
    }

    private YearMonth parseMonth(String month) {
        if (!StringUtils.hasText(month)) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "月份格式应为 yyyy-MM");
        }
    }

    private LocalDate parseDate(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private double parseOvertimeHours(String remark) {
        if (!StringUtils.hasText(remark)) {
            return 0;
        }
        Matcher matcher = OVERTIME_HOURS.matcher(remark);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private double distance(double lat1, double lng1, double lat2, double lng2) {
        double rad = Math.PI / 180;
        double dLat = (lat2 - lat1) * rad;
        double dLng = (lng2 - lng1) * rad;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1 * rad) * Math.cos(lat2 * rad)
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 6371000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private void requireRule(Long ruleId) {
        if (ruleMapper.selectById(ruleId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考勤规则不存在");
        }
    }
}