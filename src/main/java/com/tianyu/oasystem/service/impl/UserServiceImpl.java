package com.tianyu.oasystem.service.impl;

import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.PasswordDTO;
import com.tianyu.oasystem.dto.ProfileDTO;
import com.tianyu.oasystem.entity.SysUser;
import com.tianyu.oasystem.mapper.EmpEmployeeMapper;
import com.tianyu.oasystem.mapper.SysUserMapper;
import com.tianyu.oasystem.service.ConfigService;
import com.tianyu.oasystem.service.FileStorageService;
import com.tianyu.oasystem.service.UserService;
import com.tianyu.oasystem.util.UserContext;
import com.tianyu.oasystem.vo.ProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final EmpEmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final ConfigService configService;

    @Override
    public ProfileVO myProfile() {
        SysUser user = currentUser();
        ProfileVO vo = new ProfileVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setAccountCreateTime(user.getCreateTime());
        if (user.getEmpId() != null) {
            ProfileVO empProfile = employeeMapper.selectProfileByEmpId(user.getEmpId());
            if (empProfile != null) {
                vo.setEmpId(empProfile.getEmpId());
                vo.setDeptId(empProfile.getDeptId());
                vo.setEmpNo(empProfile.getEmpNo());
                vo.setName(empProfile.getName());
                vo.setGender(empProfile.getGender());
                vo.setPosition(empProfile.getPosition());
                vo.setPhone(empProfile.getPhone());
                vo.setEmail(empProfile.getEmail());
                vo.setDeptName(empProfile.getDeptName());
                vo.setHireDate(empProfile.getHireDate());
            }
        }
        return vo;
    }

    @Override
    public void updateProfile(ProfileDTO dto) {
        SysUser user = currentUser();
        if (user.getEmpId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前账号未关联员工档案，无法修改资料");
        }
        employeeMapper.updateProfile(user.getEmpId(), dto.getPhone(), dto.getEmail());
    }

    @Override
    public void updatePassword(PasswordDTO dto) {
        SysUser user = currentUser();
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "原密码错误");
        }
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "新密码不能与原密码相同");
        }
        userMapper.updatePassword(user.getUserId(), passwordEncoder.encode(dto.getNewPassword()));
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择头像文件");
        }
        long maxBytes = (long) configService.attachmentMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "头像大小超过限制（≤ " + configService.attachmentMaxSizeMb() + "MB）");
        }
        FileStorageService.StoredFile stored = fileStorageService.store(file, "avatar");
        SysUser user = currentUser();
        String old = user.getAvatarUrl();
        userMapper.updateAvatar(user.getUserId(), stored.relativePath());
        if (old != null && !old.isBlank()) {
            fileStorageService.delete(old);
        }
        return stored.relativePath();
    }

    private SysUser currentUser() {
        SysUser user = userMapper.selectById(UserContext.userId());
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号不存在");
        }
        return user;
    }
}
