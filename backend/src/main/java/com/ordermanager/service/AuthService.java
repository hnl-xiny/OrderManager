package com.ordermanager.service;

import com.ordermanager.dto.LoginRequest;
import com.ordermanager.dto.LoginResponse;

import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 退出登录
     */
    void logout(String token);

    /**
     * 获取当前用户信息
     */
    Map<String, Object> getCurrentUser(String username);
}
