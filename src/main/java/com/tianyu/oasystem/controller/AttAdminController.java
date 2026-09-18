package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.annotation.OpLog;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.AttRuleDTO;
import com.tianyu.oasystem.entity.AttRule;
import com.tianyu.oasystem.service.AttendanceService;
import com.tianyu.oasystem.util.DeptScopeUtil;
import com.tianyu.oasystem.vo.AttReportRowVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** 考勤管理（管理员；部门管理员走 /api/dept-admin 变体） */
@RestController
@RequestMapping("/api/admin/att")
@RequiredArgsConstructor
public class AttAdminController {

    private final AttendanceService attendanceService;
    private final DeptScopeUtil deptScopeUtil;

    @GetMapping("/rules")
    public Result<List<AttRule>> rules() {
        return Result.success(attendanceService.listRules());
    }

    @PostMapping("/rules")
    @OpLog(type = "ATT", biz = "考勤规则", op = "新建")
    public Result<AttRule> saveRule(@Valid @RequestBody AttRuleDTO dto) {
        return Result.success("规则已保存并生效", attendanceService.saveRule(dto));
    }

    @PutMapping("/rules/{id}")
    @OpLog(type = "ATT", biz = "考勤规则", op = "编辑")
    public Result<Void> updateRule(@PathVariable("id") Long id, @Valid @RequestBody AttRuleDTO dto) {
        attendanceService.updateRule(id, dto);
        return Result.success("规则已更新", null);
    }

    @DeleteMapping("/rules/{id}")
    @OpLog(type = "ATT", biz = "考勤规则", op = "删除")
    public Result<Void> deleteRule(@PathVariable("id") Long id) {
        attendanceService.deleteRule(id);
        return Result.success("规则已删除", null);
    }

    @PutMapping("/rules/{id}/toggle")
    public Result<Void> toggleRule(@PathVariable("id") Long id, @RequestParam Integer enabled) {
        attendanceService.toggleRule(id, enabled);
        return Result.success("规则状态已切换", null);
    }

    @GetMapping("/report-month")
    public Result<List<AttReportRowVO>> report(@RequestParam(required = false) String month) {
        return Result.success(attendanceService.monthlyReport(month, deptScopeUtil.scopedDeptIds()));
    }

    @GetMapping("/report-month/export")
    public ResponseEntity<String> export(@RequestParam(required = false) String month) {
        String csv = attendanceService.exportCsv(month, deptScopeUtil.scopedDeptIds());
        String filename = "attendance-" + (month == null ? "all" : month) + ".csv";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }
}