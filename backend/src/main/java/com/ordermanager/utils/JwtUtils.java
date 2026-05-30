package com.ordermanager.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT工具类
 *
 * 基于 jjwt 库实现 Token 的生成与验证。
 * Token 中携带 username/role/userId 信息，过期后需重新登录。
 */
@Component
public class JwtUtils {

    /** 签名密钥，从 application.yml 的 jwt.secret 注入 */
    @Value("${jwt.secret}")
    private String secret;

    /** Token有效期（毫秒），从 application.yml 的 jwt.expiration 注入 */
    @Value("${jwt.expiration}")
    private Long expiration;

    /** 请求头名称，从 application.yml 的 jwt.header 注入 */
    @Value("${jwt.header}")
    private String header;

    /** 构建签名密钥（HMAC-SHA算法要求密钥至少256bit） */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** 生成Token：填充username/role/userId声明，设置签发与过期时间 */
    public String generateToken(String username, String role, UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", role);
        claims.put("userId", userId.toString());
        return createToken(claims, username);
    }

    /** 构造JWT字符串：iat-签发时间 / exp-过期时间 */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /** 从Token中提取用户名（JWT subject） */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /** 从Token中提取角色 */
    public String getRoleFromToken(String token) {
        return (String) getClaimsFromToken(token).get("role");
    }

    /** 从Token中提取用户UUID */
    public UUID getUserIdFromToken(String token) {
        String userId = (String) getClaimsFromToken(token).get("userId");
        return UUID.fromString(userId);
    }

    /** 从Token中提取过期时间 */
    public Date getExpirationFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /** 解析Token获取全部声明 */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 判断Token是否已过期 */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /** 完整校验：签名正确且未过期 */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String getHeader() { return header; }
    public Long getExpiration() { return expiration; }
}
