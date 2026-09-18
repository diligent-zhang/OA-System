package com.tianyu.oasystem.service;

import com.tianyu.oasystem.dto.AttRuleDTO;
import com.tianyu.oasystem.dto.PunchDTO;
import com.tianyu.oasystem.entity.AttRecord;
import com.tianyu.oasystem.entity.AttRule;
import com.tianyu.oasystem.vo.AttReportRowVO;

import java.util.List;

/** 考勤管理：规则 / 打卡 / 记录 / 报表，并实现审批通过联动 */
public interface AttendanceService extends FlowAttendanceNotifier {

    List<AttRule> listRules();

    AttRule activeRule();

    AttRule saveRule(AttRuleDTO dto);

    void updateRule(Long ruleId, AttRuleDTO dto);

    void deleteRule(Long ruleId);

    void toggleRule(Long ruleId, Integer enabled);

    /** 上下班打卡，返回提示文本 */
    String punch(PunchDTO dto);

    /** 我的月度明细 */
    List<AttRecord> myMonthly(String month);

    /** 月度报表（deptIds 为空则全公司） */
    List<AttReportRowVO> monthlyReport(String month, List<Long> deptIds);

    /** 月度报表导出 CSV（UTF-8 BOM） */
    String exportCsv(String month, List<Long> deptIds);
}