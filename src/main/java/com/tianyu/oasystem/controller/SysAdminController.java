package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.annotation.OpLog;
import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.entity.SysConfig;
import com.tianyu.oasystem.entity.SysOpLog;
import com.tianyu.oasystem.entity.SysUser;
import com.tianyu.oasystem.mapper.SysUserMapper;
import com.tianyu.oasystem.service.ConfigService;
import com.tianyu.oasystem.service.OpLogService;
import com.tianyu.oasystem.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** 系统基础管理：操作日志 / 系统设置 / 角色权限（仅超级管理员） */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class SysAdminController {

    private static final Set<String> ALLOWED_ROLES = Set.of("DEPT_ADMIN", "EMPLOYEE");
    private static final Set<String> CONFIG_KEYS = Set.of("message_push_enabled", "attachment_max_size");

    private final OpLogService opLogService;
    private final ConfigService configService;
    private final SysUserMapper userMapper;

    // ==================== 操作日志 ====================

    @GetMapping("/op-logs")
    public Result<PageResult<SysOpLog>> opLogs(@RequestParam(required = false) String opType,
                                               @RequestParam(required = false) String beginTime,
                                               @RequestParam(required = false) String endTime,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(opLogService.page(null, opType, beginTime, endTime, pageNum, pageSize));
    }

    // ==================== 系统设置 ====================

    @GetMapping("/sys-config")
    public Result<List<SysConfig>> configs() {
        return Result.success(configService.all());
    }

    @PutMapping("/sys-config")
    @OpLog(type = "CONFIG", biz = "系统设置", op = "更新")
    public Result<Void> updateConfig(@RequestBody Map<String, String> body) {
        String key = body.get("configKey");
        String value = body.get("configValue");
        if (!CONFIG_KEYS.contains(key)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不允许修改该配置项");
        }
        configService.update(key, value, null);
        return Result.success("设置已保存", null);
    }

    // ==================== 角色权限 ====================

    /** 晋升/收回部门管理员 */
    @PutMapping("/users/{userId}/role")
    @OpLog(type = "AUTH", biz = "角色权限", op = "调整")
    public Result<Void> setRole(@PathVariable("userId") Long userId, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (!ALLOWED_ROLES.contains(role)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅支持 DEPT_ADMIN / EMPLOYEE 角色调整");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (user.getUserId().equals(UserContext.userId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能调整自己的角色");
        }
        if ("ADMIN".equals(user.getRole()) && !"ADMIN".equals(role)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能变更超级管理员角色");
        }
        userMapper.updateRole(userId, role);
        return Result.success("角色已更新为 " + ("DEPT_ADMIN".equals(role) ? "部门管理员" : "普通员工"), null);
    }
}