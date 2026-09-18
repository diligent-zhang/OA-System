package com.tianyu.oasystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/** 表单定义增改 */
@Data
public class FlowFormDTO {

    @NotBlank(message = "表单名称不能为空")
    private String formName;

    private String remark;

    private List<FlowFieldDTO> fields;
}