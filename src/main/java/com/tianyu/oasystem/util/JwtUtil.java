package com.tianyu.oasystem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** JWT 签发与解析 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-minutes}")
    private long expireMinutes;

    @Value("${jwt.renew-threshold-minutes}")
    private long renewThresholdMinutes;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(UserPrincipal p) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(p.getUserId()))
                .claim("username", p.getUsername())
                .claim("role", p.getRole())
                .claim("empId", p.getEmpId())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMinutes * 60_000))
                .signWith(key())
                .compact();
    }

    /** 解析 Token，无效/过期抛出 JwtException */
    public UserPrincipal parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Number empId = claims.get("empId", Number.class);
        return new UserPrincipal(
                Long.valueOf(claims.getSubject()),
                claims.get("username", String.class),
                claims.get("role", String.class),
                empId == null ? null : empId.longValue());
    }

    /** 是否临近过期需要续期 */
    public boolean needRenew(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        long remain = claims.getExpiration().getTime() - System.currentTimeMillis();
        return remain < renewThresholdMinutes * 60_000;
    }
}
