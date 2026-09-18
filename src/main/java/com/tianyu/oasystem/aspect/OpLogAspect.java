package com.tianyu.oasystem.aspect;

import com.tianyu.oasystem.annotation.OpLog;
import com.tianyu.oasystem.service.OpLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OpLogAspect {

    private final OpLogService opLogService;

    /** 注解方法执行成功后再记录（抛异常不记录，避免操作未生效却留痕） */
    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint joinPoint, OpLog opLog) throws Throwable {
        Object result = joinPoint.proceed();
        try {
            StringBuilder detail = new StringBuilder();
            if (StringUtils.hasText(opLog.op())) {
                detail.append(opLog.op());
            }
            if (StringUtils.hasText(opLog.biz())) {
                if (detail.length() > 0) {
                    detail.append(" ");
                }
                detail.append(opLog.biz());
            }
            opLogService.record(opLog.type(), opLog.biz(), null, detail.toString());
        } catch (Exception e) {
            log.warn("操作日志记录失败: {}", e.getMessage());
        }
        return result;
    }
}