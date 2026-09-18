package com.tianyu.oasystem.controller;

import com.github.pagehelper.PageHelper;
import com.tianyu.oasystem.annotation.OpLog;
import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.EmployeeDTO;
import com.tianyu.oasystem.dto.EmployeeQueryDTO;
import com.tianyu.oasystem.service.AttendanceService;
import com.tianyu.oasystem.service.EmployeeService;
import com.tianyu.oasystem.util.DeptScopeUtil;
import com.tianyu.oasystem.vo.AttReportRowVO;
import com.tianyu.oasystem.vo.EmployeeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 部门管理员端（数据范围：本部门及直属子部门），ADMIN 亦可通过（不限范围） */
@RestController
@RequestMapping("/api/dept-admin")
@RequiredArgsConstructor
public class DeptAdminController {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final DeptScopeUtil deptScopeUtil;

    // ==================== 员工管理（部门范围） ====================

    @GetMapping("/employees")
    public Result<PageResult<EmployeeVO>> page(EmployeeQueryDTO query) {
        query.setDeptIds(deptScopeUtil.scopedDeptIds());
        return Result.success(employeeService.page(query));
    }

    @GetMapping("/employees/{id}")
    public Result<EmployeeVO> detail(@PathVariable("id") Long id) {
        return Result.success(checkInScope(employeeService.getById(id)));
    }

    @PostMapping("/employees")
    @OpLog(type = "EMP", biz = "员工", op = "新增")
    public Result<Void> add(@Valid @RequestBody EmployeeDTO dto) {
        if (!inScope(dto.getDeptId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅可新增本部门员工");
        }
        employeeService.add(dto);
        return Result.success("员工新增成功，初始密码为 123456", null);
    }

    @PutMapping("/employees/{id}")
    @OpLog(type = "EMP", biz = "员工", op = "编辑")
    public Result<Void> update(@PathVariable("id") Long id, @Valid @RequestBody EmployeeDTO dto) {
        checkInScope(employeeService.getById(id));
        if (!inScope(dto.getDeptId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能将员工调整到本部门之外");
        }
        employeeService.update(id, dto);
        return Result.success("员工已更新", null);
    }

    @DeleteMapping("/employees/{id}")
    @OpLog(type = "EMP", biz = "员工", op = "删除")
    public Result<Void> delete(@PathVariable("id") Long id) {
        checkInScope(employeeService.getById(id));
        employeeService.delete(id);
        return Result.success("员工及其账号已删除", null);
    }

    @PutMapping("/employees/{id}/password/reset")
    @OpLog(type = "EMP", biz = "员工", op = "重置密码")
    public Result<Void> resetPassword(@PathVariable("id") Long id) {
        checkInScope(employeeService.getById(id));
        employeeService.resetPassword(id);
        return Result.success("密码已重置为 123456", null);
    }

    @PutMapping("/employees/{id}/status")
    @OpLog(type = "EMP", biz = "员工", op = "禁用/启用")
    public Result<Void> status(@PathVariable("id") Long id, @RequestParam Integer status) {
        checkInScope(employeeService.getById(id));
        employeeService.updateAccountStatus(id, status);
        return Result.success(status == 1 ? "账号已启用" : "账号已禁用", null);
    }

    // ==================== 考勤报表（部门范围） ====================

    @GetMapping("/attendance/report-month")
    public Result<List<AttReportRowVO>> report(@RequestParam(required = false) String month) {
        return Result.success(attendanceService.monthlyReport(month, deptScopeUtil.scopedDeptIds()));
    }

    @GetMapping("/attendance/report-month/export")
    public org.springframework.http.ResponseEntity<String> export(@RequestParam(required = false) String month) {
        String csv = attendanceService.exportCsv(month, deptScopeUtil.scopedDeptIds());
        String filename = "dept-attendance-" + (month == null ? "all" : month) + ".csv";
        String encoded = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encoded)
                .contentType(new org.springframework.http.MediaType("text", "csv",
                        java.nio.charset.StandardCharsets.UTF_8))
                .body(csv);
    }

    // ==================== 工具 ====================

    private boolean inScope(Long deptId) {
        List<Long> scope = deptScopeUtil.scopedDeptIds();
        return scope == null || (deptId != null && scope.contains(deptId));
    }

    private EmployeeVO checkInScope(EmployeeVO vo) {
        if (!inScope(vo.getDeptId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作本部门之外的员工");
        }
        return vo;
    }
}