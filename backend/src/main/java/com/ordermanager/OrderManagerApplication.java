package com.ordermanager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 订单管理系统 Spring Boot 启动类
 *
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
 * 声明此类为配置类、启用自动配置、扫描同包及子包下的组件（Controller/Service/Mapper等）
 */
@SpringBootApplication
public class OrderManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderManagerApplication.class, args);
    }
}
