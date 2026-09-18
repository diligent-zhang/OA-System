package com.tianyu.oasystem.entity;

import lombok.Data;

@Data
public class FlowNode {

    private Long nodeId;
    private Long defId;
    private String nodeName;
    private String nodeType;
    private Integer sortNo;
    private String approverType;
    private String approverIds;
    private String branchField;
    private String branchOp;
    private String branchValue;
    private Long branchToNodeId;
    private Long nextNodeId;
}