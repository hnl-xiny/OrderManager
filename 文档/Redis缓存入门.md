# Redis 缓存入门指南

## 什么是 Redis

Redis（Remote Dictionary Server）是一个**开源的内存数据结构存储系统**，常被用作：
- **缓存**（Cache）
- **消息队列**
- **分布式锁**
- **会话存储**

它将数据存储在内存中，因此读写速度极快（可达每秒数十万次操作）。

---

## 你的项目为什么需要 Redis

在没有缓存的情况下：

```
前端请求 → 查询数据库 → 返回数据
 每次都走数据库，数据库压力大，响应慢
```

加入 Redis 缓存后：

```
前端请求 → 查 Redis（命中） → 直接返回  （快！）
前端请求 → 查 Redis（未命中） → 查数据库 → 写入 Redis → 返回数据
```

对于订单列表这类查询频繁但变化不频繁的数据，缓存能大幅提升性能。

---

## 你项目中的 Redis 使用

### 1. 配置（application.yml）

```yaml
spring:
  redis:
    host: localhost        # Redis 服务地址
    port: 6379            # Redis 端口
    password:              # 密码（没设）
    database: 0            # 数据库编号
    timeout: 3000ms        # 连接超时时间
    lettuce:
      pool:
        max-active: 8      # 最大连接数
        max-idle: 8        # 最大空闲连接
        min-idle: 0        # 最小空闲连接
```

### 2. 依赖引入

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

Spring Boot 只需引入这个 starter，配置好 yml，就会自动配置 `RedisTemplate`，可以直接注入使用。

---

## Redis 数据结构

Redis 支持多种数据结构，你项目中用到了其中一种：

### String（字符串）—— 你项目主要用这个

最基本的数据结构，key-value 键值对。

```
key = "order:id:xxx"        → value = Order 对象（JSON 序列化后存储）
key = "orders:page:1:8"     → value = 分页列表数据
key = "auth:token:username" → value = JWT token
```

### Key 的命名规范

你项目中定义了统一的 key 前缀：

```java
private static final String CACHE_KEY_ORDER_DETAIL = "order:id:";     // 订单详情
private static final String CACHE_KEY_PAGE = "orders:page:";          // 订单列表
private static final String TOKEN_PREFIX = "auth:token:";             // Token 存储
```

统一前缀的好处：
- 便于管理（删除一类缓存时用通配符批量删）
- 避免 key 冲突

---

## 你项目中的缓存实现

### 1. 订单详情缓存（1小时过期）

```java
private static final String CACHE_KEY_ORDER_DETAIL = "order:id:";
private static final long CACHE_EXPIRE_DETAIL = 1;  // 小时

public Order getOrderDetail(String orderId) {
    String cacheKey = CACHE_KEY_ORDER_DETAIL + orderId;

    // 第一步：查缓存
    Order cached = (Order) redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
        return cached;  // 命中缓存，直接返回
    }

    // 第二步：缓存未命中，查数据库
    Order order = orderMapper.selectOrderDetailById(UUID.fromString(orderId));

    // 第三步：写入缓存
    redisTemplate.opsForValue().set(cacheKey, order, CACHE_EXPIRE_DETAIL, TimeUnit.HOURS);

    return order;
}
```

流程图：

```
请求 → Redis 有数据？ → 有 → 直接返回
                  → 没有 → 查数据库 → 写入Redis → 返回
```

### 2. 订单列表缓存（10分钟过期）

```java
private static final long CACHE_EXPIRE_PAGE = 10;  // 分钟

public Map<String, Object> getOrderList(OrderQueryDTO queryDTO) {
    // 缓存 key = 包含所有筛选条件
    String cacheKey = CACHE_KEY_PAGE + page + ":" + pageSize + ":" + orderType + ":" + ...;

    Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
        return cached;
    }

    // 查数据库，分页结果写入缓存
    redisTemplate.opsForValue().set(cacheKey, resultMap, CACHE_EXPIRE_PAGE, TimeUnit.MINUTES);
    return resultMap;
}
```

### 3. 写操作后清除缓存

缓存的核心问题：**数据更新后，缓存里的数据就过期了（脏数据）**。

你项目的解决方案：**写操作后主动删除缓存，让下次查询重新从数据库加载**。

```java
// 订单创建/修改/删除后，删除所有列表缓存
private void clearOrderCache() {
    var keys = redisTemplate.keys(CACHE_KEY_PAGE + "*");  // 匹配所有列表缓存 key
    if (keys != null && !keys.isEmpty()) {
        redisTemplate.delete(keys);  // 批量删除
    }
}
```

同时删除详情缓存：

```java
redisTemplate.delete(CACHE_KEY_ORDER_DETAIL + orderId);
```

### 4. Token 黑名单（退出登录）

传统 Session 方案中，用户退出登录需要服务端销毁 Session。JWT 是无状态的，服务端不知道某个 Token 是否已失效。

解决方案：**把退出登录的 Token 记到 Redis 里，验证时检查是否在黑名单中**。

```java
// 登录时存入 Redis，过期时间与 JWT 一致
redisTemplate.opsForValue().set(
    TOKEN_PREFIX + username,
    token,
    jwtUtils.getExpiration(),
    TimeUnit.MILLISECONDS
);

// 退出登录时删除
public void logout(String token) {
    String username = jwtUtils.getUsernameFromToken(token);
    redisTemplate.delete(TOKEN_PREFIX + username);
}
```

---

## 缓存策略总结

### 缓存读写模式

| 策略 | 说明 |
|------|------|
| **Cache-Aside（旁路缓存）** | 应用先查缓存，未命中再查数据库，然后写入缓存。你的项目用的就是这个模式。 |
| **Read-Through** | 应用只查缓存，缓存负责查数据库并自动加载 |
| **Write-Through** | 写入时同步更新缓存和数据库 |
| **Write-Behind** | 写入时只更新缓存，异步批量写入数据库 |

### 缓存淘汰策略

你项目中用的是**TTL（Time-To-Live）过期时间**：

| 缓存类型 | 过期时间 | 原因 |
|----------|----------|------|
| 订单详情 | 1 小时 | 数据相对稳定，不需要频繁更新 |
| 订单列表 | 10 分钟 | 筛选条件多，key 多，不宜过长 |
| Token | 24 小时 | 与 JWT 过期时间一致 |

### 缓存常见问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| **缓存穿透** | 查询不存在的数据，每次都打到数据库 | 存一个空值或布隆过滤器 |
| **缓存击穿** | 热点 key 过期，瞬间大量请求打到数据库 | 加锁 / 热点数据永不过期 |
| **缓存雪崩** | 大量 key 同时过期 | 过期时间加随机值 |

你项目目前没有针对这三个问题的特别处理，在数据量不大的情况下不是问题。

---

## Redis 常用命令（命令行操作）

```bash
# 连接 Redis
redis-cli

# 查看所有 key
KEYS *

# 查看某个 key 的值
GET order:id:xxx

# 删除某个 key
DEL order:id:xxx

# 删除所有列表缓存
KEYS orders:page:*
DEL orders:page:*

# 查看 key 剩余生存时间（秒）
TTL order:id:xxx

# 清空所有数据（慎用）
FLUSHALL
```

---

## Spring Data Redis 常用 API

你项目中使用的是 `RedisTemplate<String, Object>`：

```java
// 注入
private final RedisTemplate<String, Object> redisTemplate;

// 存值（带过期时间）
redisTemplate.opsForValue().set(key, value, 过期时间, TimeUnit);

// 取值
Object value = redisTemplate.opsForValue().get(key);

// 删除
redisTemplate.delete(key);

// 批量删除（通配符）
var keys = redisTemplate.keys("orders:page:*");
redisTemplate.delete(keys);

// 判断 key 是否存在
Boolean exists = redisTemplate.hasKey(key);

// 设置过期时间
redisTemplate.expire(key, 10, TimeUnit.MINUTES);
```

---

## 总结

```
Redis = 内存数据库 = 快速的键值存储

你项目的缓存逻辑：
  查 → 先查 Redis，有就返回，没有就查库再写入
  写 → 操作数据库后删除缓存，下次查时重新加载

好处：减少数据库压力，提升查询响应速度
代价：数据可能短暂不一致（通过写完删缓存来尽量保证一致性）
```
