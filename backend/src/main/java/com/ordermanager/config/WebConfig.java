package com.ordermanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Web配置类
 *
 * 注册全局 CORS 过滤器，对所有路由生效。
 * 与 SecurityConfig 中的 CORS 配置作用相同，此处作为兜底确保跨域请求不被浏览器拦截。
 */
@Configuration
public class WebConfig {

    /**
     * 注册 CorsFilter Bean，全局拦截所有请求并附加 CORS 响应头
     *
     * 配置说明：
     * <ul>
     *   <li>allowCredentials + allowedOriginPatterns(" * ") = 允许携带Cookie的任意来源</li>
     *   <li>maxAge=3600：预检请求（OPTIONS）结果缓存1小时，减少OPTIONS请求次数</li>
     * </ul>
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
