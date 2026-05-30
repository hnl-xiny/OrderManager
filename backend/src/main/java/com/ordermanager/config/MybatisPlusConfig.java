package com.ordermanager.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MyBatis-Plus 配置
 *
 * 1. 注册分页插件（适配 PostgreSQL）<br>
 * 2. 实现 MetaObjectHandler，自动填充 createdAt/updatedAt 及 UUID 主键
 */
@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {

    /** 注册分页拦截器，目标数据库为 PostgreSQL */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }

    /** 插入时自动填充策略 */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 实体有 orderId 字段且为null时，自动生成UUID
        if (metaObject.hasGetter("orderId") && metaObject.getValue("orderId") == null) {
            fillStrategy(metaObject, "orderId", UUID.randomUUID());
        }
        fillStrategy(metaObject, "createdAt", LocalDateTime.now());
        fillStrategy(metaObject, "updatedAt", LocalDateTime.now());
    }

    /** 更新时自动填充 updatedAt */
    @Override
    public void updateFill(MetaObject metaObject) {
        fillStrategy(metaObject, "updatedAt", LocalDateTime.now());
    }
}
