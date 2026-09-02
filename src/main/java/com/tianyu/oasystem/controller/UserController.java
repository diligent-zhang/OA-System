package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.PasswordDTO;
import com.tianyu.oasystem.dto.ProfileDTO;
import com.tianyu.oasystem.service.UserService;
import com.tianyu.oasystem.vo.ProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 个人中心（两端共用） */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<ProfileVO> profile() {
        return Result.success(userService.myProfile());
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody ProfileDTO dto) {
        userService.updateProfile(dto);
        return Result.success("资料已更新", null);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordDTO dto) {
        userService.updatePassword(dto);
        return Result.success("密码修改成功，请重新登录", null);
    }
}
