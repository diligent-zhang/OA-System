package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.PasswordDTO;
import com.tianyu.oasystem.dto.ProfileDTO;
import com.tianyu.oasystem.entity.SysOpLog;
import com.tianyu.oasystem.service.OpLogService;
import com.tianyu.oasystem.service.UserService;
import com.tianyu.oasystem.vo.ProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** 个人中心（两端共用）+ 头像 + 我的操作记录 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OpLogService opLogService;

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

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(file);
        return Result.success("头像已更新", avatarUrl);
    }

    /** 个人操作记录 */
    @GetMapping("/op-logs")
    public Result<PageResult<SysOpLog>> myOpLogs(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) String opType) {
        return Result.success(opLogService.myPage(opType, pageNum, pageSize));
    }
}