package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.SysUser;
import org.apache.ibatis.annotations.Param;

public interface SysUserMapper {

    SysUser selectByUsername(@Param("username") String username);

    SysUser selectById(@Param("userId") Long userId);

    SysUser selectByEmpId(@Param("empId") Long empId);

    int countByUsername(@Param("username") String username);

    int insert(SysUser user);

    int updatePassword(@Param("userId") Long userId, @Param("password") String password);

    /** 按员工 ID 更新账号状态（禁用/启用） */
    int updateStatusByEmpId(@Param("empId") Long empId, @Param("status") Integer status);

    /** 按员工 ID 重置密码 */
    int updatePasswordByEmpId(@Param("empId") Long empId, @Param("password") String password);

    int deleteByEmpId(@Param("empId") Long empId);

    int updateRole(@Param("userId") Long userId, @Param("role") String role);

    int updateAvatar(@Param("userId") Long userId, @Param("avatarUrl") String avatarUrl);

    /** 全部启用中的用户ID（站内信广播用） */
    java.util.List<Long> selectAllActiveUserIds();

    /** 启用中的账号数（公告应读人数） */
    int countActive();

    /** 全部启用中的账号（公告未读名单） */
    java.util.List<String> selectAllActiveUsernames();

    /** 指定角色下启用中的用户ID（审批人 ROLE 方式） */
    java.util.List<Long> selectUserIdsByRole(@Param("role") String role);

    /** 指定部门下的部门管理员用户ID（审批人 DEPT_LEADER 方式） */
    java.util.List<Long> selectDeptAdminUserIds(@Param("deptId") Long deptId);

    /** 指定部门下启用中的用户ID（审批人 OWN_DEPT 方式，联查员工表） */
    java.util.List<Long> selectUserIdsByDeptId(@Param("deptId") Long deptId);
}
