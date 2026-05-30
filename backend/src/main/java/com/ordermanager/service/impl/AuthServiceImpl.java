package com.ordermanager.service.impl;

import com.ordermanager.dto.LoginRequest;
import com.ordermanager.dto.LoginResponse;
import com.ordermanager.entity.SysUserRole;
import com.ordermanager.mapper.SysUserRoleMapper;
import com.ordermanager.service.AuthService;
import com.ordermanager.utils.JwtUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 *
 * 测试账号硬编码在 USERS 中，生产环境应改为从数据库读取。
 * Token 存入 Redis 以支持主动失效（退出登录）。
 */
@Service
public class AuthServiceImpl implements AuthService {

    /** 内存测试账号：key=username, value="password|role" */
    private static final Map<String, String> USERS = new HashMap<>();
    /** Redis中存储Token的key前缀：auth:token:{username} */
    private static final String TOKEN_PREFIX = "auth:token:";

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SysUserRoleMapper sysUserRoleMapper;

    static {
        // 测试账号（实际生产环境应从数据库读取）
        USERS.put("admin", "123456|admin");
        USERS.put("user", "123456|operator");
    }

    public AuthServiceImpl(JwtUtils jwtUtils, RedisTemplate<String, Object> redisTemplate,
                           SysUserRoleMapper sysUserRoleMapper) {
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    /**
     * 用户登录：验证账号密码 → 生成JWT → 存入Redis
     *
     * @return 包含Token、用户名、角色、用户ID和过期时间的响应体
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // 验证用户
        String userInfo = USERS.get(username);
        if (userInfo == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        String[] parts = userInfo.split("\\|");
        if (!parts[0].equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        String role = parts[1];
        SysUserRole user = sysUserRoleMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在，请联系管理员");
        }
        String token = jwtUtils.generateToken(username, role, user.getUserId());

        // 将Token存入Redis，过期时间与JWT一致，支持退出时主动删除
        redisTemplate.opsForValue().set(
            TOKEN_PREFIX + username,
            token,
            jwtUtils.getExpiration(),
            TimeUnit.MILLISECONDS
        );

        return new LoginResponse(token, username, role, user.getUserId(), jwtUtils.getExpiration());
    }

    /** 退出登录：从Redis中移除Token，使服务端Token立即失效 */
    @Override
    public void logout(String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        redisTemplate.delete(TOKEN_PREFIX + username);
    }

    /** 获取当前用户信息（从内存读取，未走数据库） */
    @Override
    public Map<String, Object> getCurrentUser(String username) {
        Map<String, Object> userInfo = new HashMap<>();
        String userInfoStr = USERS.get(username);

        if (userInfoStr != null) {
            String[] parts = userInfoStr.split("\\|");
            userInfo.put("username", username);
            userInfo.put("role", parts[1]);
        }

        return userInfo;
    }
}
