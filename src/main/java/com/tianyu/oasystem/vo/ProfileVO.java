package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 个人中心档案视图 */
@Data
public class ProfileVO {

    private Long userId;
    private String username;
    private String role;
    private String avatarUrl;

    private Long empId;
    private Long deptId;
    private String empNo;
    private String name;
    private Integer gender;
    private String position;
    private String phone;
    private String email;
    private String deptName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime accountCreateTime;
}
