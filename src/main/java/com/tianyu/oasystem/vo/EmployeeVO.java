package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/** 员工列表视图（含部门名与登录账号状态） */
@Data
public class EmployeeVO {

    private Long empId;
    private String empNo;
    private String name;
    private Integer gender;
    private String phone;
    private String email;
    private Long deptId;
    private String deptName;
    /** 岗位 */
    private String position;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    /** 在职状态 1在职 0离职 */
    private Integer status;

    /** 登录账号ID */
    private Long userId;
    /** 登录账号 */
    private String username;
    /** 账号状态 1正常 0禁用 */
    private Integer accountStatus;
    /** 账号角色 */
    private String role;
}
