package com.ordermanager.dto;

import lombok.Data;

import java.util.UUID;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    /** JWT Token，客户端需在后续请求的 Authorization Header 中携带 */
    private String token;
    /** 用户名 */
    private String username;
    /** 角色：admin-管理员 / operator-操作员 */
    private String role;
    /** 用户唯一标识 */
    private UUID userId;
    /** Token 有效期（毫秒） */
    private Long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(String token, String username, String role, UUID userId, Long expiresIn) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.userId = userId;
        this.expiresIn = expiresIn;
    }
}
