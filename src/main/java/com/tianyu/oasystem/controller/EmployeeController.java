package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.EmployeeDTO;
import com.tianyu.oasystem.dto.EmployeeQueryDTO;
import com.tianyu.oasystem.service.EmployeeService;
import com.tianyu.oasystem.vo.EmployeeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 员工信息管理（管理员端，/api/admin 前缀由拦截器做角色校验） */
@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public Result<PageResult<EmployeeVO>> page(EmployeeQueryDTO query) {
        return Result.success(employeeService.page(query));
    }

    @GetMapping("/{id}")
    public Result<EmployeeVO> detail(@PathVariable("id") Long id) {
        return Result.success(employeeService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody EmployeeDTO dto) {
        employeeService.add(dto);
        return Result.success("员工新增成功，初始密码为 123456", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @Valid @RequestBody EmployeeDTO dto) {
        employeeService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        employeeService.delete(id);
        return Result.success("员工及其账号已删除", null);
    }

    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> empIds) {
        employeeService.deleteBatch(empIds);
        return Result.success("批量删除成功", null);
    }

    @PutMapping("/{id}/password/reset")
    public Result<Void> resetPassword(@PathVariable("id") Long id) {
        employeeService.resetPassword(id);
        return Result.success("密码已重置为 123456", null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateAccountStatus(@PathVariable("id") Long id, @RequestParam Integer status) {
        employeeService.updateAccountStatus(id, status);
        return Result.success(status == 1 ? "账号已启用" : "账号已禁用", null);
    }
}
