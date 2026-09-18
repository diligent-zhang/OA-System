package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.annotation.OpLog;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.FlowHandleDTO;
import com.tianyu.oasystem.dto.FlowSubmitDTO;
import com.tianyu.oasystem.service.FlowDefService;
import com.tianyu.oasystem.service.FlowInstanceService;
import com.tianyu.oasystem.vo.FlowDefVO;
import com.tianyu.oasystem.vo.FlowDetailVO;
import com.tianyu.oasystem.vo.FlowListVO;
import com.tianyu.oasystem.vo.FlowTodoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 审批发起与办理（登录即可） */
@RestController
@RequestMapping("/api/flow")
@RequiredArgsConstructor
public class FlowController {

    private final FlowDefService defService;
    private final FlowInstanceService instanceService;

    /** 可用流程（员工发起选择） */
    @GetMapping("/defs")
    public Result<java.util.List<FlowDefVO>> enabledDefs() {
        return Result.success(defService.listDefs(false));
    }

    @GetMapping("/defs/{id}")
    public Result<FlowDefVO> defDetail(@PathVariable("id") Long id) {
        return Result.success(defService.getDef(id));
    }

    // ==================== 发起与查询 ====================

    @PostMapping("/instances")
    @OpLog(type = "FLOW", biz = "审批单", op = "提交")
    public Result<Long> submit(@Valid @RequestBody FlowSubmitDTO dto) {
        return Result.success("审批已提交", instanceService.submit(dto));
    }

    @GetMapping("/instances/mine")
    public Result<PageResult<FlowListVO>> mine(@RequestParam(required = false) String status,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(instanceService.myPage(status, keyword, pageNum, pageSize));
    }

    @GetMapping("/instances/my-todo")
    public Result<PageResult<FlowTodoVO>> todo(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(instanceService.todoPage(pageNum, pageSize));
    }

    @GetMapping("/instances/done")
    public Result<PageResult<FlowTodoVO>> done(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(instanceService.donePage(pageNum, pageSize));
    }

    @GetMapping("/instances/{id}")
    public Result<FlowDetailVO> detail(@PathVariable("id") Long id) {
        return Result.success(instanceService.detail(id));
    }

    @PutMapping("/instances/{id}/withdraw")
    @OpLog(type = "FLOW", biz = "审批单", op = "撤回")
    public Result<Void> withdraw(@PathVariable("id") Long id) {
        instanceService.withdraw(id);
        return Result.success("已撤回", null);
    }

    // ==================== 任务办理 ====================

    @PutMapping("/tasks/{taskId}/approve")
    @OpLog(type = "FLOW", biz = "审批单", op = "同意")
    public Result<Void> approve(@PathVariable("taskId") Long taskId, @RequestBody(required = false) FlowHandleDTO dto) {
        instanceService.approve(taskId, dto);
        return Result.success("已同意", null);
    }

    @PutMapping("/tasks/{taskId}/reject")
    @OpLog(type = "FLOW", biz = "审批单", op = "驳回")
    public Result<Void> reject(@PathVariable("taskId") Long taskId, @RequestBody(required = false) FlowHandleDTO dto) {
        instanceService.reject(taskId, dto);
        return Result.success("已驳回", null);
    }

    @PutMapping("/tasks/{taskId}/transfer")
    @OpLog(type = "FLOW", biz = "审批单", op = "转办")
    public Result<Void> transfer(@PathVariable("taskId") Long taskId, @RequestBody(required = false) FlowHandleDTO dto) {
        instanceService.transfer(taskId, dto);
        return Result.success("已转办", null);
    }

    @PostMapping("/instances/{id}/comment")
    @OpLog(type = "FLOW", biz = "审批单", op = "补充意见")
    public Result<Void> comment(@PathVariable("id") Long id, @RequestBody(required = false) FlowHandleDTO dto) {
        instanceService.comment(id, dto);
        return Result.success("意见已添加", null);
    }
}