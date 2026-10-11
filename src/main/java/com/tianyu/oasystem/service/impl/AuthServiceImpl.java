package com.tianyu.oasystem.service.impl;

import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.LoginDTO;
import com.tianyu.oasystem.dto.RegisterDTO;
import com.tianyu.oasystem.entity.EmpEmployee;
import com.tianyu.oasystem.entity.SysUser;
import com.tianyu.oasystem.mapper.EmpEmployeeMapper;
import com.tianyu.oasystem.mapper.SysDeptMapper;
import com.tianyu.oasystem.mapper.SysUserMapper;
import com.tianyu.oasystem.service.AuthService;
import com.tianyu.oasystem.service.OpLogService;
import com.tianyu.oasystem.util.GenerateUtil;
import com.tianyu.oasystem.util.JwtUtil;
import com.tianyu.oasystem.util.UserPrincipal;
import com.tianyu.oasystem.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final EmpEmployeeMapper employeeMapper;
    private final SysDeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OpLogService opLogService;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }

        String name = "系统管理员";
        Long deptId = null;
        if (user.getEmpId() != null) {
            EmpEmployee emp = employeeMapper.selectById(user.getEmpId());
            if (emp != null) {
                name = emp.getName();
                deptId = emp.getDeptId();
            }
        }

        UserPrincipal principal = new UserPrincipal(
                user.getUserId(), user.getUsername(), user.getRole(), user.getEmpId(), deptId);
        String token = jwtUtil.createToken(principal);
        opLogService.record("LOGIN", "AUTH", String.valueOf(user.getUserId()), "用户登录成功");
        return new LoginVO(token, user.getUserId(), user.getUsername(), user.getRole(),
                user.getEmpId(), name, user.getAvatarUrl());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        if (userMapper.countByUsername(dto.getUsername()) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名已存在，请更换");
        }
        checkLeafDept(dto.getDeptId());

        EmpEmployee employee = new EmpEmployee();
        employee.setEmpNo(GenerateUtil.nextEmpNo(employeeMapper));
        employee.setName(dto.getName());
        employee.setGender(dto.getGender());
        employee.setPhone(dto.getPhone());
        employee.setEmail(dto.getEmail());
        employee.setDeptId(dto.getDeptId());
        if (dto.getPosition() != null && !dto.getPosition().isBlank()) {
            employee.setPosition(dto.getPosition());
        } else {
            employee.setPosition("");
        }
        employee.setHireDate(dto.getHireDate() == null ? LocalDate.now() : dto.getHireDate());
        employee.setStatus(1);
        employeeMapper.insert(employee);

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("EMPLOYEE");
        user.setEmpId(employee.getEmpId());
        user.setStatus(1);
        userMapper.insert(user);
        opLogService.record("AUTH", "REGISTER", String.valueOf(user.getUserId()), "新用户注册");
    }

    @Override
    public boolean usernameExists(String username) {
        return userMapper.countByUsername(username) > 0;
    }

    /** 员工应挂在末级部门下 */
    private void checkLeafDept(Long deptId) {
        if (deptId == null) {
            return;
        }
        if (deptMapper.selectById(deptId) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "所选部门不存在");
        }
        if (deptMapper.countChildren(deptId) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择具体子部门，不能挂在一级部门下");
        }
    }
}
