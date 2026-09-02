package com.tianyu.oasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeptDTO {

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    @NotNull(message = "请选择上级部门")
    private Long parentId;

    private Integer sortNo = 0;
}
