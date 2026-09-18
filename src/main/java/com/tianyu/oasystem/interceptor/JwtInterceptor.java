package com.tianyu.oasystem.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.util.JwtUtil;
import com.tianyu.oasystem.util.UserContext;
import com.tianyu.oasystem.util.UserPrincipal;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/** JWT 鉴权拦截器：校验 Token 有效性与角色权限 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            return reject(response, ResultCode.UNAUTHORIZED, "未登录或登录已过期，请重新登录");
        }
        String token = auth.substring(7);

        UserPrincipal principal;
        try {
            principal = jwtUtil.parseToken(token);
        } catch (JwtException | IllegalArgumentException e) {
            return reject(response, ResultCode.UNAUTHORIZED, "登录状态无效，请重新登录");
        }

        // 越权访问控制：/api/admin 仅超级管理员；/api/dept-admin 允许部门管理员及以上
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/admin") && !principal.isAdmin()) {
            return reject(response, ResultCode.FORBIDDEN, "权限不足，仅超级管理员可操作");
        }
        if (uri.startsWith("/api/dept-admin")
                && !(principal.isAdmin() || "DEPT_ADMIN".equals(principal.getRole()))) {
            return reject(response, ResultCode.FORBIDDEN, "权限不足，需部门管理员及以上");
        }

        // Token 自动续期：临近过期时通过响应头下发新 Token
        if (jwtUtil.needRenew(token)) {
            response.setHeader("X-Renew-Token", jwtUtil.createToken(principal));
        }

        UserContext.set(principal);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean reject(HttpServletResponse response, Integer code, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, message)));
        return false;
    }
}
