package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.annotation.OpLog;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.FlowDefDTO;
import com.tianyu.oasystem.dto.FlowFormDTO;
import com.tianyu.oasystem.service.FlowDefService;
import com.tianyu.oasystem.service.FlowFormAdminService;
import com.tianyu.oasystem.service.FlowInstanceService;
import com.tianyu.oasystem.vo.FlowDefVO;
import com.tianyu.oasystem.vo.FlowFormVO;
import com.tianyu.oasystem.vo.FlowListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 审批配置与归档（仅超级管理员） */
@RestController
@RequestMapping("/api/admin/flow")
@RequiredArgsConstructor
public class FlowAdminController {

    private final FlowFormAdminService formService;
    private final FlowDefService defService;
    private final FlowInstanceService instanceService;

    // ==================== 表单定义 ====================

    @GetMapping("/forms")
    public Result<List<FlowFormVO>> forms() {
        return Result.success(formService.listAll());
    }

    @GetMapping("/forms/{id}")
    public Result<FlowFormVO> formDetail(@PathVariable("id") Long id) {
        return Result.success(formService.getForm(id));
    }

    @PostMapping("/forms")
    @OpLog(type = "FLOW", biz = "审批表单", op = "新建")
    public Result<Long> createForm(@Valid @RequestBody FlowFormDTO dto) {
        return Result.success("表单创建成功", formService.createForm(dto));
    }

    @PutMapping("/forms/{id}")
    @OpLog(type = "FLOW", biz = "审批表单", op = "编辑")
    public Result<Void> updateForm(@PathVariable("id") Long id, @Valid @RequestBody FlowFormDTO dto) {
        formService.updateForm(id, dto);
        return Result.success("表单已更新", null);
    }

    @DeleteMapping("/forms/{id}")
    @OpLog(type = "FLOW", biz = "审批表单", op = "删除")
    public Result<Void> deleteForm(@PathVariable("id") Long id) {
        formService.deleteForm(id);
        return Result.success("表单已删除", null);
    }

    @PutMapping("/forms/{id}/toggle")
    public Result<Void> toggleForm(@PathVariable("id") Long id) {
        formService.toggleForm(id);
        return Result.success("表单状态已切换", null);
    }

    // ==================== 流程定义 ====================

    @GetMapping("/defs")
    public Result<List<FlowDefVO>> defs() {
        return Result.success(defService.listDefs(true));
    }

    @GetMapping("/defs/{id}")
    public Result<FlowDefVO> defDetail(@PathVariable("id") Long id) {
        return Result.success(defService.getDef(id));
    }

    @PostMapping("/defs")
    @OpLog(type = "FLOW", biz = "审批流程", op = "新建")
    public Result<Long> createDef(@Valid @RequestBody FlowDefDTO dto) {
        return Result.success("流程创建成功", defService.createDef(dto));
    }

    @PutMapping("/defs/{id}")
    @OpLog(type = "FLOW", biz = "审批流程", op = "编辑")
    public Result<Void> updateDef(@PathVariable("id") Long id, @Valid @RequestBody FlowDefDTO dto) {
        defService.updateDef(id, dto);
        return Result.success("流程已更新", null);
    }

    @DeleteMapping("/defs/{id}")
    @OpLog(type = "FLOW", biz = "审批流程", op = "删除")
    public Result<Void> deleteDef(@PathVariable("id") Long id) {
        defService.deleteDef(id);
        return Result.success("流程已删除", null);
    }

    @PutMapping("/defs/{id}/toggle")
    public Result<Void> toggleDef(@PathVariable("id") Long id) {
        defService.toggleDef(id);
        return Result.success("流程状态已切换", null);
    }

    // ==================== 审批归档 ====================

    @GetMapping("/instances")
    public Result<PageResult<FlowListVO>> archive(@RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String category,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(instanceService.archivePage(status, category, keyword, pageNum, pageSize));
    }
}