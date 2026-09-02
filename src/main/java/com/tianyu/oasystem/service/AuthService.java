package com.tianyu.oasystem.service;

import com.tianyu.oasystem.dto.LoginDTO;
import com.tianyu.oasystem.dto.RegisterDTO;
import com.tianyu.oasystem.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    /** 员工注册：同步创建员工档案 + 默认 EMPLOYEE 角色账号 */
    void register(RegisterDTO dto);

    boolean usernameExists(String username);
}
