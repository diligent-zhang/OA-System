package com.tianyu.oasystem.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 操作日志注解：标注在 Controller 方法上，AOP 切面自动落库 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpLog {

    /** 操作类型：LOGIN/FLOW/FILE/NOTICE/DEPT/EMP/CONFIG/ATT/DOC/AUTH */
    String type();

    /** 业务对象，如 请假申请/公告 */
    String biz() default "";

    /** 动作，如 提交/同意/发布 */
    String op() default "";
}