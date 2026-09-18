package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.dto.EmployeeQueryDTO;
import com.tianyu.oasystem.entity.EmpEmployee;
import com.tianyu.oasystem.vo.EmployeeVO;
import com.tianyu.oasystem.vo.ProfileVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmpEmployeeMapper {

    /** 多条件分页查询（部门名、账号信息联查） */
    List<EmployeeVO> selectPage(EmployeeQueryDTO query);

    EmployeeVO selectVOById(@Param("empId") Long empId);

    EmpEmployee selectById(@Param("empId") Long empId);

    ProfileVO selectProfileByEmpId(@Param("empId") Long empId);

    int countByDeptId(@Param("deptId") Long deptId);

    /** 在职员工简要列表（考勤报表/通讯录用；deptIds 为空不限） */
    List<EmployeeVO> selectActiveList(@Param("deptIds") List<Long> deptIds);

    int countAll();

    int countOnJob();

    /** 查询某前缀下最大工号，用于生成新员工工号 */
    String selectMaxEmpNo(@Param("prefix") String prefix);

    int insert(EmpEmployee employee);

    int update(EmpEmployee employee);

    /** 个人资料修改（手机号/邮箱） */
    int updateProfile(@Param("empId") Long empId, @Param("phone") String phone, @Param("email") String email);

    int deleteById(@Param("empId") Long empId);

    int deleteByIds(@Param("ids") List<Long> ids);

    /** 部门人员占比统计（子部门归并到一级部门） */
    List<com.tianyu.oasystem.vo.NameValueVO> selectDeptEmpStats();
}
