package com.tianyu.oasystem.dto;

import lombok.Data;

/** 流程节点定义（流程配置用；nextIdx/branchToIdx 为节点列表下标，-1 表示结束） */
@Data
public class FlowNodeDTO {

    private String nodeName;
    private String nodeType;
    /** ROLE/DEPT_LEADER/SPECIFIC_USER/OWN_DEPT */
    private String approverType;
    /** ROLE: 角色值；SPECIFIC_USER: 用户ID */
    private String approverIds;
    /** 分支条件字段 */
    private String branchField;
    private String branchOp;
    private String branchValue;
    private int branchToIdx = -1;
    private int nextIdx = -1;
}