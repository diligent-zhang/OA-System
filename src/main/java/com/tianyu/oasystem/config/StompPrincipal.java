package com.tianyu.oasystem.config;

import java.security.Principal;

/** STOMP 会话身份：getName() 返回用户ID字符串，供 /user/queue 定向推送使用 */
public class StompPrincipal implements Principal {

    private final Long userId;
    private final String username;

    public StompPrincipal(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
