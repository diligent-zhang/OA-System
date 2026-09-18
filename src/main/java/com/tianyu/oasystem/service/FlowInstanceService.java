package com.tianyu.oasystem.service;

import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.dto.FlowHandleDTO;
import com.tianyu.oasystem.dto.FlowSubmitDTO;
import com.tianyu.oasystem.vo.FlowDetailVO;
import com.tianyu.oasystem.vo.FlowListVO;
import com.tianyu.oasystem.vo.FlowTodoVO;

/** 审批实例与任务处理（流程引擎） */
public interface FlowInstanceService {

    /** 发起审批 */
    Long submit(FlowSubmitDTO dto);

    /** 我发起的 */
    PageResult<FlowListVO> myPage(String status, String keyword, int pageNum, int pageSize);

    /** 待我审批 */
    PageResult<FlowTodoVO> todoPage(int pageNum, int pageSize);

    /** 我已审批 */
    PageResult<FlowTodoVO> donePage(int pageNum, int pageSize);

    /** 管理归档 */
    PageResult<FlowListVO> archivePage(String status, String category, String keyword,
                                       int pageNum, int pageSize);

    FlowDetailVO detail(Long instanceId);

    /** 撤回（审批中） */
    void withdraw(Long instanceId);

    void approve(Long taskId, FlowHandleDTO dto);

    void reject(Long taskId, FlowHandleDTO dto);

    void transfer(Long taskId, FlowHandleDTO dto);

    void comment(Long instanceId, FlowHandleDTO dto);

    /** 我的待办数（门户聚合用） */
    int countTodo();

    /** 我发起且审批中的单据数（门户聚合用） */
    int countProcessing();
}