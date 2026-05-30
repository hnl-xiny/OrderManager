package com.ordermanager.handler;

import com.ordermanager.dto.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT认证失败入口处理器
 *
 * 实现 Spring Security 的 AuthenticationEntryPoint 接口。
 * 当请求未携带有效Token或认证失败时，由Security自动调用此方法，
 * 向客户端返回JSON格式的401未授权响应。
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /** 未认证时返回JSON错误体（绕过Spring Security默认的跳转行为） */
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\",\"timestamp\":" + System.currentTimeMillis() + "}");
    }
}
