package com.ordermanager.handler;

import com.ordermanager.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 *
 * 继承 OncePerRequestFilter，确保每个请求最多执行一次。
 * 拦截流程：提取Header中的Bearer Token → 验证签名与有效期 → 设置SecurityContext。
 * 验证失败不阻断请求链（由SecurityConfig的AuthenticationEntryPoint统一处理）。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 核心过滤逻辑：提取、验证Token，构建认证上下文
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            // Token存在且有效时，构建Spring Security认证对象并放入上下文
            if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);

                // credentials 存放 userId（UUID），供Controller层直接获取
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        username,
                        jwtUtils.getUserIdFromToken(token),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("JWT认证失败: " + e.getMessage());
        }

        // 继续过滤器链（无论认证成功与否，请求都会继续）
        filterChain.doFilter(request, response);
    }

    /** 从请求头提取 Bearer Token */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtUtils.getHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
