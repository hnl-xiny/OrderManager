package com.ordermanager.controller;

import com.ordermanager.dto.LoginRequest;
import com.ordermanager.dto.LoginResponse;
import com.ordermanager.dto.Result;
import com.ordermanager.service.AuthService;
import com.ordermanager.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthService authService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 退出登录
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtUtils.getHeader());
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            authService.logout(token);
        }
        return Result.success("退出成功", null);
    }

    /**
     * 获取当前用户信息
     * GET /api/auth/current
     */
    @GetMapping("/current")
    public Result<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return Result.unauthorized("未登录");
        }
        return Result.success(authService.getCurrentUser(authentication.getName()));
    }
}
