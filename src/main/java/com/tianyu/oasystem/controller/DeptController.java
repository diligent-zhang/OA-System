package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.DeptDTO;
import com.tianyu.oasystem.service.DeptService;
import com.tianyu.oasystem.vo.DeptVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 部门管理：/api/admin/depts 为管理员 CRUD；/api/depts/tree 为两端公共查询 */
@RestController
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @GetMapping("/api/admin/depts")
    public Result<PageResult<DeptVO>> page(@RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(deptService.page(keyword, pageNum, pageSize));
    }

    @GetMapping("/api/depts/tree")
    public Result<List<DeptVO>> tree() {
        return Result.success(deptService.tree());
    }

    @PostMapping("/api/admin/depts")
    public Result<Void> add(@Valid @RequestBody DeptDTO dto) {
        deptService.add(dto);
        return Result.success("部门新增成功", null);
    }

    @PutMapping("/api/admin/depts/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @Valid @RequestBody DeptDTO dto) {
        deptService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/api/admin/depts/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        deptService.delete(id);
        return Result.success("部门已删除", null);
    }
}
