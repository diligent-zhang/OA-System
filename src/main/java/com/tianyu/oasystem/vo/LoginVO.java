package com.tianyu.oasystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;
    private Long userId;
    private String username;
    private String role;
    private Long empId;
    /** 员工姓名（管理员无档案时为“系统管理员”） */
    private String name;
}
