package com.tianyu.oasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 文件夹授权 */
@Data
public class DocFolderPermDTO {

    @NotBlank(message = "缺失权限类型")
    @Pattern(regexp = "READ|EDIT", message = "权限类型必须为 READ 或 EDIT")
    private String permType;

    @NotBlank(message = "缺失授权对象类型")
    @Pattern(regexp = "DEPT|USER", message = "授权对象类型必须为 DEPT 或 USER")
    private String grantType;

    @NotNull(message = "缺失授权对象ID")
    private Long grantId;
}