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
}
