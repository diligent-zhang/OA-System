package com.tianyu.oasystem.service.impl;

import com.github.pagehelper.PageHelper;
import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.EmployeeDTO;
import com.tianyu.oasystem.dto.EmployeeQueryDTO;
import com.tianyu.oasystem.entity.EmpEmployee;
import com.tianyu.oasystem.entity.SysUser;
import com.tianyu.oasystem.mapper.EmpEmployeeMapper;
import com.tianyu.oasystem.mapper.SysDeptMapper;
import com.tianyu.oasystem.mapper.SysUserMapper;
import com.tianyu.oasystem.service.EmployeeService;
import com.tianyu.oasystem.util.GenerateUtil;
import com.tianyu.oasystem.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final EmpEmployeeMapper employeeMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<EmployeeVO> page(EmployeeQueryDTO query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        return PageResult.of(employeeMapper.selectPage(query));
    }

    @Override
    public EmployeeVO getById(Long empId) {
        EmployeeVO vo = employeeMapper.selectVOById(empId);
        if (vo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "员工不存在");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(EmployeeDTO dto) {
        if (!StringUtils.hasText(dto.getUsername())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请为员工设置登录账号");
        }
        if (userMapper.countByUsername(dto.getUsername()) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "登录账号已存在，请更换");
        }
        if (deptMapper.selectById(dto.getDeptId()) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "所选部门不存在");
        }
        if (deptMapper.countChildren(dto.getDeptId()) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "员工须归属具体子部门，不能挂在一级部门下");
        }

        EmpEmployee employee = new EmpEmployee();
        employee.setEmpNo(GenerateUtil.nextEmpNo(employeeMapper));
        employee.setName(dto.getName());
        employee.setGender(dto.getGender());
        employee.setPhone(dto.getPhone());
        employee.setEmail(dto.getEmail());
        employee.setDeptId(dto.getDeptId());
        employee.setPosition(dto.getPosition() == null ? "" : dto.getPosition());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        employeeMapper.insert(employee);

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole("EMPLOYEE");
        user.setEmpId(employee.getEmpId());
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public void update(Long empId, EmployeeDTO dto) {
        requireEmployee(empId);
        EmpEmployee employee = new EmpEmployee();
        employee.setEmpId(empId);
        employee.setName(dto.getName());
        employee.setGender(dto.getGender());
        employee.setPhone(dto.getPhone());
        employee.setEmail(dto.getEmail());
        employee.setDeptId(dto.getDeptId());
        employee.setPosition(dto.getPosition());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        employeeMapper.update(employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long empId) {
        requireEmployee(empId);
        userMapper.deleteByEmpId(empId);
        employeeMapper.deleteById(empId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> empIds) {
        if (empIds == null || empIds.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择要删除的员工");
        }
        empIds.forEach(userMapper::deleteByEmpId);
        employeeMapper.deleteByIds(empIds);
    }

    @Override
    public void resetPassword(Long empId) {
        requireEmployee(empId);
        SysUser user = userMapper.selectByEmpId(empId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "该员工暂无登录账号");
        }
        userMapper.updatePassword(user.getUserId(), passwordEncoder.encode(DEFAULT_PASSWORD));
    }

    @Override
    public void updateAccountStatus(Long empId, Integer status) {
        requireEmployee(empId);
        SysUser user = userMapper.selectByEmpId(empId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "该员工暂无登录账号");
        }
        userMapper.updateStatusByEmpId(empId, status);
    }

    private void requireEmployee(Long empId) {
        if (employeeMapper.selectById(empId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "员工不存在");
        }
    }
}
