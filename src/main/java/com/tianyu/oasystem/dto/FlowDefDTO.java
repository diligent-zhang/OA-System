package com.tianyu.oasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 流程定义增改 */
@Data
public class FlowDefDTO {

    @NotNull(message = "请选择表单")
    private Long formId;

    @NotBlank(message = "流程名称不能为空")
    private String defName;

    /** leave/overtime/travel/requisition/custom */
    private String category = "custom";

    /** 审批节点（按顺序） */
    private List<FlowNodeDTO> nodes;
}