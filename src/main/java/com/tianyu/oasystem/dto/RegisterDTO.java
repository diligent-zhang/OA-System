package com.tianyu.oasystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/** 员工自主注册：同步创建员工档案与登录账号 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{3,19}$",
            message = "用户名以字母开头，长度 4~20 位，仅含字母、数字、下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)\\S{6,20}$",
            message = "密码长度 6~20 位，须同时包含字母和数字")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 30, message = "姓名长度不能超过 30 个字符")
    private String name;

    @NotNull(message = "请选择性别")
    private Integer gender;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private Long deptId;

    /** 岗位（可选） */
    private String position;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;
}
