package com.tianyu.oasystem.util;

/** 基于 ThreadLocal 的当前登录用户上下文 */
public class UserContext {

    private static final ThreadLocal<UserPrincipal> HOLDER = new ThreadLocal<>();

    public static void set(UserPrincipal principal) {
        HOLDER.set(principal);
    }

    public static UserPrincipal get() {
        return HOLDER.get();
    }

    public static Long userId() {
        return get().getUserId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
