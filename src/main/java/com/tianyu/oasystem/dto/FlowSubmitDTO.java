package com.tianyu.oasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/** 发起审批 */
@Data
public class FlowSubmitDTO {

    @NotNull(message = "请选择审批流程")
    private Long defId;

    @NotBlank(message = "申请标题不能为空")
    private String title;

    /** 表单数据 {fieldKey: value} */
    private Map<String, Object> formData;
}