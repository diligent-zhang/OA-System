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
    private Long deptId;          // 员工所属部门ID（管理员为 null 表示不限范围）

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /** 部门数据范围：ADMIN 不限，DEPT_ADMIN 或 EMPLOYEE 限定本部门 */
    public boolean deptScoped() {
        return !isAdmin() && deptId != null;
    }
}
