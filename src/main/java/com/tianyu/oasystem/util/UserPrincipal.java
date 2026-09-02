package com.tianyu.oasystem.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 当前登录用户（JWT 解析结果） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {

    private Long userId;
    private String username;
    private String role;
    private Long empId;

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
