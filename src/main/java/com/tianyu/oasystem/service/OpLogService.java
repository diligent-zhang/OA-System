package com.tianyu.oasystem.service;

import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.entity.SysOpLog;

public interface OpLogService {

    /** 记录一条操作日志（当前登录用户 + 当前请求 IP） */
    void record(String opType, String bizType, String bizId, String detail);

    /** 管理员多条件分页查询 */
    PageResult<SysOpLog> page(Long userId, String opType, String beginTime, String endTime,
                              int pageNum, int pageSize);

    /** 个人操作记录 */
    PageResult<SysOpLog> myPage(String opType, int pageNum, int pageSize);
}