package com.tianyu.oasystem.vo;

import lombok.Data;

/** 通讯录联系人（对外只暴露必要信息） */
@Data
public class ContactVO {

    private Long empId;
    /** 关联登录账号ID（转办等场景需要） */
    private Long userId;
    private String empNo;
    private String name;
    private Integer gender;
    private String position;
    private String phone;
    private String email;
    private Long deptId;
    private String deptName;
}