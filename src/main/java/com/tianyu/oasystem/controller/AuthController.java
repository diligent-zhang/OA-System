package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.LoginDTO;
import com.tianyu.oasystem.dto.RegisterDTO;
import com.tianyu.oasystem.service.AuthService;
import com.tianyu.oasystem.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success("登录成功", authService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success("注册成功，请使用新账号登录", null);
    }

    @GetMapping("/username-exists")
    public Result<Boolean> usernameExists(@RequestParam String username) {
        return Result.success(authService.usernameExists(username));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("已退出登录", null);
    }
}
