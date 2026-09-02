package com.tianyu.oasystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/** 管理员新增/编辑员工 */
@Data
public class EmployeeDTO {

    /** 新增时必填：登录账号用户名；编辑时忽略 */
    @Pattern(regexp = "^$|^[a-zA-Z][a-zA-Z0-9_]{3,19}$",
            message = "用户名以字母开头，长度 4~20 位，仅含字母、数字、下划线")
    private String username;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotNull(message = "请选择性别")
    private Integer gender;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "请选择所属部门")
    private Long deptId;

    @NotNull(message = "请选择入职日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @NotNull(message = "请选择在职状态")
    private Integer status;
}
