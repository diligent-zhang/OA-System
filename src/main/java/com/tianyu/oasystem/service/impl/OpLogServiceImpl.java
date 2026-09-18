package com.tianyu.oasystem.service.impl;

import com.github.pagehelper.PageHelper;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.entity.SysOpLog;
import com.tianyu.oasystem.mapper.SysOpLogMapper;
import com.tianyu.oasystem.service.OpLogService;
import com.tianyu.oasystem.util.IpUtil;
import com.tianyu.oasystem.util.UserContext;
import com.tianyu.oasystem.util.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OpLogServiceImpl implements OpLogService {

    private final SysOpLogMapper opLogMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String opType, String bizType, String bizId, String detail) {
        SysOpLog log = new SysOpLog();
        UserPrincipal principal = UserContext.get();
        if (principal != null) {
            log.setUserId(principal.getUserId());
            log.setUsername(principal.getUsername());
        }
        log.setOpType(opType);
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setDetail(detail);
        log.setIp(IpUtil.currentIp());
        opLogMapper.insert(log);
    }

    @Override
    public PageResult<SysOpLog> page(Long userId, String opType, String beginTime, String endTime,
                                     int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(opLogMapper.selectPage(userId, opType, beginTime, endTime));
    }

    @Override
    public PageResult<SysOpLog> myPage(String opType, int pageNum, int pageSize) {
        return page(UserContext.userId(), opType, null, null, pageNum, pageSize);
    }
}