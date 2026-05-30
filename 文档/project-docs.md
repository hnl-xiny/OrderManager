# OrderManager 项目详细文档

## 一、项目概述

OrderManager（订单管理系统）是一套前后端分离的全栈应用，用于管理客户、设备和订单的业务流程。系统支持用户登录认证、订单的创建/编辑/审核/删除、客户和设备的查询与管理。

---

## 二、技术栈总览

### 后端

| 分类 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.2.5 |
| 运行环境 | Java | 21 |
| ORM 框架 | MyBatis-Plus | 3.5.5 |
| 数据库 | PostgreSQL | - |
| 缓存 | Redis | - |
| 安全认证 | Spring Security + JWT (jjwt) | 6.x / 0.12.5 |
| 工具库 | Hutool、Lombok | 5.8.29 / - |
| JSON 处理 | Jackson JSR310 | - |

### 前端

| 分类 | 技术 | 版本 |
|------|------|------|
| UI 框架 | React | 18.3.1 |
| 构建工具 | Vite | 5.3.1 |
| 状态管理 | Redux Toolkit | 2.2.6 |
| 路由 | react-router-dom | 6.24.0 |
| UI 组件库 | Ant Design (antd) | 5.18.0 |
| HTTP 客户端 | axios | 1.7.2 |
| 日期处理 | dayjs | 1.11.11 |

---

## 三、后端类详细说明

### 3.1 启动类

#### `com.ordermanager.OrderManagerApplication`

Spring Boot 应用的入口类。通过 `@SpringBootApplication` 注解启用自动配置、组件扫描和 Spring Boot 的默认行为。

### 3.2 配置类（config/）

#### `SecurityConfig`

Spring Security 配置类，负责应用的安全策略。

**主要配置项**：
- **密码编码器**：`BCryptPasswordEncoder`，用于用户登录时密码比对。
- **CSRF 禁用**：前后端分离架构中 JWT 无状态认证不需要 CSRF 防护。
- **CORS 配置**：允许所有来源、所有请求头和所有方法的跨域请求。
- **Session 策略**：`STATELESS`，不创建 HTTP Session，完全依赖 JWT Token。
- **认证入口**：`JwtAuthenticationEntryPoint`，未登录或 Token 失效时返回 401 JSON 响应。
- **JWT 过滤器**：在 `UsernamePasswordAuthenticationFilter` 之前插入 `JwtAuthenticationFilter`，从请求头提取并验证 Token。
- **路径权限**：`/auth/**` 路径放行（登录相关接口无需认证），其余接口需登录后访问。

#### `RedisConfig`

Redis 模板配置类，配置了 Redis 的序列化方式。

**关键点**：
- **Key 序列化**：使用 `StringRedisSerializer`，所有 Redis Key 以字符串形式存储。
- **Value 序列化**：使用 `GenericJackson2JsonRedisSerializer` 配合自定义 `ObjectMapper`，注册 `JavaTimeModule` 并禁用时间戳格式，确保 `LocalDateTime`、`LocalDate` 等 Java 8 时间类型能正确序列化/反序列化。

#### `WebConfig`

全局 Web 配置，通过 `CorsFilter` Bean 提供跨域支持。

**CORS 策略**：允许携带凭证、允许所有来源和所有请求方法、预检请求缓存 3600 秒。

#### `MybatisPlusConfig`

MyBatis-Plus 全局配置，同时实现了 `MetaObjectHandler` 接口用于自动填充。

**分页插件**：`PaginationInnerInterceptor`，指定数据库类型为 PostgreSQL，实现物理分页。

**自动填充策略**（通过 `MetaObjectHandler`）：
- `insertFill`：插入时自动填充 `orderId`（UUID）、`createdAt`、`updatedAt`。
- `updateFill`：更新时自动填充 `updatedAt`。

### 3.3 控制器层（controller/）

#### `AuthController`

认证控制器，处理登录、退出和获取当前用户信息。

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 登录 | POST | `/api/auth/login` | 验证用户名密码，生成 JWT 返回给前端 |
| 退出 | POST | `/api/auth/logout` | 从 Redis 删除 Token，清除会话 |
| 当前用户 | GET | `/api/auth/current` | 从 Security 上下文获取当前登录用户信息 |

#### `OrderController`

订单管理 REST 控制器，提供订单的增删改查和状态管理接口。

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 订单列表 | GET | `/api/orders` | 分页查询，支持多条件筛选（类型、状态、日期、关键词） |
| 订单详情 | GET | `/api/orders/{id}` | 根据 ID 查询单个订单（含关联客户、设备信息） |
| 创建订单 | POST | `/api/orders` | 新增订单，支持重复订单覆盖逻辑 |
| 更新订单 | PUT | `/api/orders/{id}` | 更新订单，仅限 `pending` 状态 |
| 更新状态 | PUT | `/api/orders/{id}/status` | 审核/取消审核（admin/operator 权限） |
| 删除订单 | DELETE | `/api/orders` | 批量软删除，仅删除 `pending` 状态订单 |
| 统计接口 | GET | `/api/orders/equipment-stats` | 统计各设备关联的订单数量，异常单独捕获返回友好提示 |
| 近30天销售订单 | GET | `/api/orders/audited-sales` | 获取近30天已审核的销售订单（按金额倒序） |

#### `CustomerController`

客户控制器，提供客户列表查询。

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 客户列表 | GET | `/api/customers` | 支持按客户名称或联系电话模糊搜索 |

#### `EquipmentController`

设备控制器，提供设备列表查询。

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 设备列表 | GET | `/api/equipment` | 支持按状态筛选，默认返回全部 |

### 3.4 服务层（service/）

#### `OrderService`（接口）

订单服务接口，定义了订单相关的业务方法签名：
- `getOrderList`：分页查询订单列表（带缓存）
- `getOrderDetail`：查询订单详情（带缓存）
- `createOrder`：创建订单（含今日重复订单覆盖逻辑）
- `updateOrder`：更新订单
- `updateOrderStatus`：审核/取消审核
- `deleteOrders`：批量软删除
- `getRecentAuditedSalesOrders`：近30天销售订单统计
- `countOrdersByEquipment`：设备订单统计

#### `OrderServiceImpl`（实现类）

订单服务实现类，核心业务逻辑所在。

**缓存策略**：
- `order:id:{orderId}` — 订单详情，1 小时过期
- `orders:page:{条件hash}` — 分页列表，10 分钟过期
- `orders:check:{customerId}:{equipmentId}` — 重复订单检查，24 小时过期

**今日重复订单覆盖逻辑**（`createOrder` 方法）：
1. 验证客户和设备状态是否正常。
2. 调用 `selectActiveDuplicate` 查询今日是否存在相同客户和设备的活跃订单。
3. 若存在，则将该订单的字段更新为新数据，`orderStatus` 重置为 `pending`，并设置 `overwritten=true`。
4. 若不存在，则正常插入新订单。
5. 操作后清除订单相关缓存。

**状态流转校验**：
- `pending` → `approved`（审核通过）
- `approved` → `pending`（取消审核）
- 其他转换路径均抛出 `BusinessException`。

**权限控制**：审核、取消审核、删除操作需要 `ROLE_admin` 或 `ROLE_operator` 权限。

**乐观更新**：审核操作在前端采用乐观更新模式，先更新本地状态，接口失败时回滚。

#### `CustomerService`（接口）

客户列表查询接口，支持关键词模糊搜索。

#### `CustomerServiceImpl`（实现类）

客户列表查询实现类。
- 缓存 key：`customers`，6 小时过期。
- 缓存策略：仅无搜索关键词时缓存完整客户列表；有关键词时每次查询数据库。
- 搜索条件：`status='normal'` + 按 `customer_name` 或 `contact_phone` 模糊匹配。

#### `EquipmentService`（接口）

设备列表查询接口，支持按状态筛选。

#### `EquipmentServiceImpl`（实现类）

设备列表查询实现类。
- 缓存 key：`equipment:normal`，6 小时过期。
- 缓存策略：仅筛选 `status='normal'` 时使用缓存，其余情况查询数据库。

#### `AuthService`（接口）

认证服务接口：
- `login`：用户登录
- `logout`：退出登录
- `getCurrentUser`：获取当前用户信息

#### `AuthServiceImpl`（实现类）

认证服务实现类。

**用户验证**：从内存中的 `USERS` Map（静态代码块初始化，包含 `admin` 和 `user` 两个测试账号）验证用户名密码，然后查询 `sys_user_role` 表获取用户角色和 UUID。

**Token 管理**：
- 登录成功后调用 `JwtUtils.generateToken()` 生成 JWT。
- 将 Token 存入 Redis，key 格式为 `auth:token:{username}`，TTL 与 JWT 过期时间一致（24 小时）。
- 退出时从 Redis 删除对应 Token。

### 3.5 数据访问层（mapper/）

#### `OrderMapper`

继承 `BaseMapper<Order>`，额外定义了以下自定义查询方法：

| 方法 | 说明 |
|------|------|
| `selectOrderPage` | 分页查询订单（关联客户、设备、创建人信息），支持多条件筛选 |
| `selectOrderDetailById` | 根据 ID 查询订单详情（含关联字段） |
| `selectByIdMapped` | 根据 ID 查询，走 XML resultMap 处理 UUID |
| `selectActiveDuplicate` | 查询今日同客户同设备的活跃订单 |
| `selectByIdsMapped` | 批量查询，走 XML resultMap |
| `updateOrderById` | 自定义更新，使用内联 typeHandler 处理 UUID |
| `countOrdersByEquipment` | 统计各设备关联的订单数量（仅 normal 设备） |
| `selectRecentAuditedSalesOrders` | 近30天已审核销售订单（按金额倒序取前10条） |

#### `CustomerMapper`

继承 `BaseMapper<Customer>`，使用 MyBatis-Plus 自动生成的 CRUD 方法。

#### `EquipmentMapper`

继承 `BaseMapper<Equipment>`，使用 MyBatis-Plus 自动生成的 CRUD 方法。

#### `SysUserRoleMapper`

继承 `BaseMapper<SysUserRole>`，额外定义了 `selectByUsername` 方法（XML 自定义 SQL），根据用户名查询用户角色信息。

### 3.6 实体类（entity/）

#### `Order`

订单实体类，对应数据库 `orders` 表。

**字段说明**：

| 字段 | 类型 | 说明 | 数据库列 |
|------|------|------|----------|
| orderId | UUID | 订单主键 | order_id (bytea) |
| customerId | UUID | 客户外键 | customer_id (bytea) |
| equipmentId | UUID | 设备外键 | equipment_id (bytea) |
| orderType | String | 订单类型（purchase/sales） | order_type |
| orderAmount | BigDecimal | 订单金额 | order_amount |
| deliveryDate | LocalDate | 交付日期 | delivery_date |
| orderStatus | String | 订单状态（pending/approved/shipped/completed） | order_status |
| remarks | String | 备注 | remarks |
| createdBy | UUID | 创建人外键 | created_by (bytea) |
| createdAt | LocalDateTime | 创建时间（自动填充） | created_at |
| updatedAt | LocalDateTime | 更新时间（自动填充） | updated_at |
| lastModifiedAt | LocalDateTime | 最近修改时间 | last_modified_at |
| deleted | Boolean | 逻辑删除标记（@TableLogic） | deleted |
| customerName | String | 关联字段（不映射数据库） | - |
| customerPhone | String | 关联字段 | - |
| equipmentName | String | 关联字段 | - |
| equipmentSpec | String | 关联字段 | - |
| creatorName | String | 关联字段 | - |
| overwritten | Boolean | 标记是否为覆盖更新（前端展示用） | - |

**关键注解**：
- `@TableName(value = "orders", autoResultMap = true)`：`autoResultMap = true` 使实体类上的 typeHandler 在 XML resultMap 中也生效。
- `@TableLogic`：逻辑删除注解，删除时自动将 `deleted` 字段设为 `true`。

#### `Customer`

客户实体类，对应 `customers` 表。

| 字段 | 类型 | 说明 |
|------|------|------|
| customerId | UUID | 客户主键 |
| customerName | String | 客户名称 |
| contactPerson | String | 联系人 |
| contactPhone | String | 联系电话 |
| status | String | 状态（normal/disabled） |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

#### `Equipment`

设备实体类，对应 `equipment` 表。

| 字段 | 类型 | 说明 |
|------|------|------|
| equipmentId | UUID | 设备主键 |
| equipmentCode | String | 设备编号（唯一） |
| equipmentName | String | 设备名称 |
| specification | String | 规格型号 |
| status | String | 状态（normal/repair/disabled） |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

#### `SysUserRole`

用户角色实体类，对应 `sys_user_role` 表。

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | UUID | 用户主键 |
| username | String | 用户名（唯一） |
| roleName | String | 角色名（admin/operator） |
| roleDesc | String | 角色描述 |
| status | String | 状态 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 3.7 DTO 类（dto/）

#### `LoginRequest`

登录请求体，仅包含 `username` 和 `password` 两个字段。

#### `LoginResponse`

登录响应体，包含：
- `token`：JWT 令牌字符串
- `username`：用户名
- `role`：角色名
- `userId`：用户 UUID
- `expiresIn`：过期时间（毫秒）

#### `OrderRequest`

订单创建/更新请求体，包含完整的订单字段及以下校验注解：

| 字段 | 校验规则 |
|------|----------|
| customerId | `@NotNull`，客户不能为空 |
| equipmentId | `@NotNull`，设备不能为空 |
| orderType | `@NotBlank`，类型不能为空 |
| orderAmount | `@NotNull`，`@DecimalMin("0.00")`，`@Digits(integer=8, fraction=2)` |
| deliveryDate | `@NotNull`，`@FutureOrPresent`（不能小于今天） |
| remarks | 无校验，可选 |
| forceCreate | 强制创建标记，跳过重复检查（默认 false） |

#### `OrderQueryDTO`

订单查询参数封装，包含分页参数（page, pageSize）和筛选条件（orderType, orderStatus, startDate, endDate, keyword）。

#### `Result<T>`

统一响应结果封装类，所有 Controller 接口的返回值均使用此类包装。

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码（200成功，400参数错误，401未登录，403无权限，409冲突，500服务器错误） |
| message | String | 提示信息 |
| data | T | 响应数据 |
| timestamp | Long | 响应时间戳 |

提供静态工厂方法：`success()`、`success(data)`、`success(msg, data)`、`error()`、`badRequest()`、`unauthorized()`、`forbidden()`、`conflict()`。

### 3.8 处理器（handler/）

#### `GlobalExceptionHandler`

全局异常处理器（`@RestControllerAdvice`），统一处理所有未被捕获的异常并返回标准化 JSON 响应。

| 异常类型 | HTTP 状态码 | 处理逻辑 |
|----------|-------------|----------|
| `MethodArgumentNotValidException` | 400 | 参数校验异常，提取所有字段错误信息逗号拼接 |
| `BindException` | 400 | 参数绑定异常，同上 |
| `BusinessException` | 400 | 业务异常，直接返回 message |
| `PermissionDeniedException` | 403 | 权限不足异常 |
| `DuplicateOrderException` | 409 | 重复订单异常 |
| `MyBatisSystemException` | 500 | MyBatis 系统异常，提取 cause 信息 |
| `ClassCastException` | 400 | 类型转换异常，当消息含 `LinkedHashMap` 和 `Order` 时返回友好提示"今日已存在相同客户和设备的订单，该订单已被覆盖更新" |
| `Exception` | 500 | 通用异常，打印堆栈并返回原始错误信息 |

内部类定义：
- `BusinessException`：业务异常
- `PermissionDeniedException`：权限不足异常
- `DuplicateOrderException`：重复订单异常

#### `JwtAuthenticationFilter`

JWT 认证过滤器（`OncePerRequestFilter`），每个请求都会经过此过滤器。

**处理流程**：
1. 从 HTTP 请求头获取 `Authorization` 字段，提取 `Bearer {token}` 中的 token 部分。
2. 调用 `JwtUtils.validateToken()` 验证 token 签名和有效期。
3. 验证通过后，解析 token 中的 `username`、`role`、`userId` 构建 `UsernamePasswordAuthenticationToken`。
4. 将认证信息存入 `SecurityContextHolder`，供后续 Controller 或 Security 决策使用。
5. 验证失败或 token 不存在时不影响请求继续传递。

#### `JwtAuthenticationEntryPoint`

JWT 认证失败入口点（实现 `AuthenticationEntryPoint`），当请求未携带有效 Token 访问受保护资源时被调用。

**响应**：返回 HTTP 401 状态码，Content-Type 为 `application/json`，返回 `{code:401, message:"未登录或token已过期", timestamp:...}`。

#### `UUIDTypeHandler`

MyBatis TypeHandler，实现 `TypeHandler<UUID>` 接口，负责 Java `UUID` 类型与 PostgreSQL `uuid`（或 `bytea`）类型之间的双向转换。

- **写入**：`setParameter` — 将 Java UUID 转为数据库 UUID 类型（`ps.setObject`）。
- **读取**：`getResult` — 将数据库 UUID 转为 Java UUID（通过 `rs.getObject(column, UUID.class)`）。

### 3.9 工具类（utils/）

#### `JwtUtils`

JWT 工具类，提供 Token 的生成、解析和验证功能。

**配置项**（从 `application.yml` 注入）：
- `jwt.secret`：签名密钥（HMAC-SHA256，最少 256 位）
- `jwt.expiration`：过期时间（毫秒，默认 86400000 = 24 小时）
- `jwt.header`：请求头名称（默认 `Authorization`）

**核心方法**：

| 方法 | 说明 |
|------|------|
| `generateToken(username, role, userId)` | 生成 Token，将 username、role、userId 存入 claims |
| `getUsernameFromToken(token)` | 从 Token 中获取用户名（subject） |
| `getRoleFromToken(token)` | 从 Token 中获取角色 |
| `getUserIdFromToken(token)` | 从 Token 中获取用户 UUID |
| `getExpirationFromToken(token)` | 从 Token 中获取过期时间 |
| `isTokenExpired(token)` | 判断 Token 是否已过期 |
| `validateToken(token)` | 验证 Token（签名有效且未过期） |

Token 采用 JJWT 0.12.5 版本构建，签名算法为 HMAC-SHA256。

### 3.10 MyBatis XML 映射文件

#### `OrderMapper.xml`

定义了 `OrderMapper` 接口对应的 SQL 语句：

| SQL ID | 类型 | 说明 |
|---------|------|------|
| `OrderDetailMap` | resultMap | 订单结果映射，定义所有字段与列的对应关系（含 UUID typeHandler） |
| `selectOrderPage` | select | 分页查询订单列表，带动态条件（if test），关联客户/设备/创建人表 |
| `selectByIdMapped` | select | 根据 ID 查询，使用 resultMap |
| `selectActiveDuplicate` | select | 查询今日重复订单，使用 resultMap |
| `selectByIdsMapped` | select | 批量查询，使用 resultMap 和 foreach |
| `selectOrderDetailById` | select | 订单详情（含关联额外字段 contactPerson、equipmentCode） |
| `countOrdersByEquipment` | select | 统计各设备订单数量（排除 completed/cancelled） |
| `selectRecentAuditedSalesOrders` | select | 近30天已审核销售订单（关联查询，按金额倒序） |
| `updateOrderById` | update | 更新订单，使用内联 typeHandler 处理所有 UUID 字段 |

**动态 SQL**：使用 `<if test>` 实现条件筛选，使用 `<foreach>` 实现批量查询的 IN 子句。

#### `SysUserRoleMapper.xml`

定义了 `SysUserRoleMapper.selectByUsername` 的 SQL：

| SQL ID | 类型 | 说明 |
|---------|------|------|
| `BaseResultMap` | resultMap | 用户角色结果映射（含 UUID typeHandler） |
| `selectByUsername` | select | 根据用户名查询用户角色信息 |

---

## 四、前端类详细说明

### 4.1 入口与路由

#### `main.jsx`

React 应用入口文件。

- 通过 `ReactDOM.createRoot` 挂载根组件。
- 包裹 `<Provider store={store}>`（Redux 状态管理）。
- 包裹 `<BrowserRouter>`（React Router 路由管理）。

#### `App.jsx`

应用路由配置文件，使用 `react-router-dom` 的 `Routes` 和 `Route` 配置路由。

**路由结构**：
- `/login` — 登录页（公开访问）
- `/` — 受保护的布局路由，需登录
  - `/dashboard` — 数据概览
  - `/customers` — 客户管理
  - `/orders` — 订单管理
  - `/sales` — 销售跟进
  - `/equipment` — 设备管理

**路由守卫**：`PrivateRoute` 组件检查 Redux 中的 `isAuthenticated` 状态，未登录则重定向到 `/login`。

### 4.2 状态管理（store/）

#### `store/index.js`

Redux Store 配置文件。

**持久化策略**：使用 `localStorage` 持久化 `auth` slice 的状态（token 和用户信息），实现页面刷新后登录状态不丢失。

**中间件**：使用 Redux Toolkit 默认的 `redux-thunk`（通过 `configureStore` 隐式启用）。

**Store 结构**：
- `auth`：认证状态（token、userInfo、role、isAuthenticated）
- `order`：订单状态（列表、详情、分页、筛选条件、loading、error）
- `ui`：UI 状态（侧边栏折叠状态、当前模块、弹窗可见性、编辑中订单）

#### `store/slices/authSlice.js`

认证状态切片，管理用户的登录状态。

**初始状态**：
```js
{
  token: localStorage.getItem('token') || null,
  userInfo: null,
  role: null,
  isAuthenticated: false,
}
```

**Action**：
- `setCredentials`：登录成功时调用，填充 token、userInfo、role、isAuthenticated，并写入 localStorage。
- `clearCredentials`：退出登录时调用，清除所有状态和 localStorage。
- `setUserInfo`：设置用户信息。

**Selector**：
- `selectAuth`：获取整个 auth 状态
- `selectIsAuthenticated`：获取是否已认证
- `selectUserRole`：获取用户角色
- `selectToken`：获取 JWT token

#### `store/slices/orderSlice.js`

订单状态切片，管理订单列表和操作的全局状态。

**初始状态**：
- `orders`：订单列表数组
- `currentOrder`：当前查看的订单详情
- `pagination`：分页信息（total、current、pageSize）
- `filters`：筛选条件（orderType、orderStatus、startDate、endDate、keyword）
- `loading`：加载状态
- `error`：错误信息

**AsyncThunk**（异步操作）：
- `fetchOrders`：获取订单列表
- `fetchOrderDetail`：获取订单详情
- `createOrder`：创建订单
- `updateOrder`：更新订单
- `updateOrderStatus`：更新订单状态
- `deleteOrders`：删除订单

**Reducer**（同步操作）：
- `setOrders`：设置订单列表
- `setFilters`：更新筛选条件（同时重置页码为1）
- `updateOrderOptimistic`：乐观更新订单状态（审核时先更新本地）
- `clearFilters`：清除筛选条件
- `setPagination`：设置分页参数
- `clearCurrentOrder`：清除当前订单
- `clearError`：清除错误信息

**Selector**：
- `selectOrders`、`selectCurrentOrder`、`selectPagination`、`selectFilters`、`selectOrderLoading`、`selectOrderError`

#### `store/slices/uiSlice.js`

UI 状态切片，管理侧边栏、弹窗等界面状态。

**初始状态**：
- `sidebarCollapsed`：侧边栏是否折叠
- `currentModule`：当前模块标识
- `modalVisible`：各弹窗可见性（orderForm、orderDetail、confirm）
- `editingOrder`：正在编辑的订单（null 表示新建）

**Action**：
- `toggleSidebar` / `setSidebarCollapsed`：侧边栏折叠控制
- `setCurrentModule`：设置当前模块
- `openOrderFormModal` / `closeOrderFormModal`：订单表单弹窗
- `openOrderDetailModal` / `closeOrderDetailModal`：订单详情弹窗
- `openConfirmModal` / `closeConfirmModal`：确认弹窗

### 4.3 网络层（services/）

#### `services/api.js`

封装 axios 实例和所有 API 调用方法。

**axios 实例配置**：
- `baseURL`：`/api`（通过 Vite 代理转发到 `http://localhost:8080`）
- `timeout`：30000 毫秒

**请求拦截器**：从 Redux Store 获取 token，附加到 `Authorization: Bearer {token}` 请求头。

**响应拦截器**：
- `401`：Token 失效，清除认证状态，跳转登录页。
- `403`：提示"权限不足"。
- 非 GET 请求失败：自动重试（最多 2 次）。
- `showError !== false` 时：使用 Ant Design `message.error` 展示错误信息。

**API 方法**：

| 对象 | 方法 | 说明 |
|------|------|------|
| `authApi` | login、logout、getCurrentUser | 认证相关 |
| `orderApi` | getOrderList、getOrderDetail、createOrder、updateOrder、updateOrderStatus、deleteOrders | 订单相关 |
| `customerApi` | getCustomerList | 客户相关 |
| `equipmentApi` | getEquipmentList | 设备相关 |

### 4.4 页面组件（pages/）

#### `pages/Login.jsx`

登录页面。

- 使用 Ant Design `Form` 构建登录表单，包含用户名和密码输入框。
- 调用 `authApi.login` 验证用户。
- 登录成功后将 token、username、role 存入 Redux（触发 `setCredentials`）。
- 跳转到 `/dashboard`。
- 登录失败显示错误提示。
- 页面背景采用渐变色，展示测试账号信息。

#### `pages/Dashboard.jsx`

数据概览页面。

- 四个统计卡片：订单总数、待审核数量、已完成数量、订单总金额。
- 数据通过调用 `orderApi.getOrderList` 并在前端聚合计算。
- 使用 Ant Design `Statistic` 组件展示数字统计。

#### `pages/CustomerManagement.jsx`

客户管理页面。

- 展示客户列表表格（Ant Design `Table`）。
- 支持按客户名称或联系电话搜索（实时搜索，带防抖）。
- 表格列：客户名称、联系人、联系电话、状态（Tag）、创建时间。
- 状态使用彩色 Tag 区分（normal=绿色、disabled=红色）。

#### `pages/OrderManagement.jsx`

订单管理页面（最核心的页面组件）。

**功能**：
- 订单列表展示（Table），支持分页、排序。
- 筛选栏：关键词搜索、订单类型下拉、订单状态下拉、日期范围选择。
- 刷新按钮重新加载当前筛选条件下的数据。

**操作列权限**：
- **所有人**：查看详情
- **admin**：审核/取消审核、编辑、删除（单个和批量）

**新建/编辑订单弹窗**：
- 选择客户（下拉搜索）、选择设备（下拉搜索）、订单类型、订单金额、交付日期、备注。
- 表单使用 Ant Design `Form` 和 `DatePicker`（禁用过往日期）。
- 提交时格式化日期为 `YYYY-MM-DD` 字符串。

**订单详情弹窗**：
- 展示订单完整信息（编号、客户、设备、金额、状态、创建/更新时间等）。
- 无操作按钮，仅查看。

**重复订单处理**：
- 创建订单时若后端返回 409（DuplicateOrderException），弹出确认框询问是否覆盖。
- 用户确认后以 `forceCreate=true` 参数重新提交。

**乐观更新**：
- 审核操作先立即更新本地状态，后台接口调用失败时回滚到之前的状态。

#### `pages/SalesFollowup.jsx`

销售跟进页面。

- 使用 Ant Design `Timeline` 组件展示最近 10 条订单。
- 每个时间节点展示客户名、设备、金额、交付日期、创建时间。
- 颜色根据订单状态变化（completed=绿色、shipped=蓝色、其余=灰色）。

#### `pages/EquipmentManagement.jsx`

设备管理页面。

- 三个统计卡片：正常数量、维修中数量、已停用数量。
- 设备卡片列表，左侧用颜色条标识状态（normal=绿色、repair=黄色、disabled=红色）。
- 每个卡片展示设备名称、编号、规格、状态。

### 4.5 布局组件（components/）

#### `components/Layout.jsx`

应用主布局组件。

**结构**：
- `Sider`（左侧导航栏）：可折叠，包含系统标题和导航菜单。
- `Header`（顶部栏）：折叠按钮 + 用户头像下拉菜单。
- `Content`（内容区）：通过 `<Outlet />` 渲染子路由页面。

**导航菜单项**：数据概览、客户管理、订单管理、销售跟进、设备管理。

**用户菜单**：显示当前角色，点击退出登录（调用 `authApi.logout`，清除 Redux 状态，跳转登录页）。

**权限展示**：头像显示 `A`（admin）或 `U`（普通用户）。

---

## 五、数据库设计

### 5.1 表结构

#### `sys_user_role`（用户角色表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| user_id | UUID | PRIMARY KEY | 用户主键 |
| username | VARCHAR(50) | UNIQUE | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码（前端未加密传输，后端目前未 BCrypt） |
| role_name | VARCHAR(50) | NOT NULL | 角色名（admin/operator） |
| role_desc | TEXT | | 角色描述 |
| status | VARCHAR(20) | DEFAULT 'normal' | 状态 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

索引：username 唯一索引、role_name 普通索引、status 普通索引。

#### `customers`（客户表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| customer_id | UUID | PRIMARY KEY | 客户主键 |
| customer_name | VARCHAR(100) | UNIQUE NOT NULL | 客户名称 |
| contact_person | VARCHAR(100) | | 联系人 |
| contact_phone | VARCHAR(50) | UNIQUE NOT NULL | 联系电话 |
| status | VARCHAR(20) | DEFAULT 'normal' | 状态（normal/disabled） |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

索引：customer_name 唯一索引、contact_phone 唯一索引、status 普通索引。

#### `equipment`（设备表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| equipment_id | UUID | PRIMARY KEY | 设备主键 |
| equipment_code | VARCHAR(50) | UNIQUE NOT NULL | 设备编号 |
| equipment_name | VARCHAR(100) | NOT NULL | 设备名称 |
| specification | VARCHAR(200) | | 规格型号 |
| status | VARCHAR(20) | DEFAULT 'normal' | 状态（normal/repair/disabled） |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

索引：equipment_code 唯一索引、status 普通索引。

#### `orders`（订单表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| order_id | UUID | PRIMARY KEY | 订单主键 |
| customer_id | UUID | FK → customers | 客户外键 |
| equipment_id | UUID | FK → equipment | 设备外键 |
| order_type | VARCHAR(20) | NOT NULL | 订单类型（purchase/sales） |
| order_amount | NUMERIC(10,2) | NOT NULL | 订单金额 |
| delivery_date | DATE | NOT NULL | 交付日期 |
| order_status | VARCHAR(20) | DEFAULT 'pending' | 状态（pending/approved/shipped/completed） |
| remarks | TEXT | | 备注 |
| created_by | UUID | FK → sys_user_role | 创建人外键 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |
| last_modified_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 最近修改时间 |
| deleted | BOOLEAN | DEFAULT FALSE | 逻辑删除标记 |

索引：
- `(customer_id, equipment_id, DATE(created_at))` 联合索引，支持重复订单查询
- `order_status` 条件索引（仅未删除记录）
- `created_at DESC` 降序索引

---

## 六、API 接口汇总

### 认证接口

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/login` | 否 | 用户登录 |
| POST | `/api/auth/logout` | 是 | 退出登录 |
| GET | `/api/auth/current` | 是 | 获取当前用户信息 |

### 订单接口

| 方法 | 路径 | 认证 | 权限 | 说明 |
|------|------|------|------|------|
| GET | `/api/orders` | 是 | - | 分页查询订单 |
| GET | `/api/orders/{id}` | 是 | - | 订单详情 |
| POST | `/api/orders` | 是 | - | 创建订单 |
| PUT | `/api/orders/{id}` | 是 | - | 更新订单 |
| PUT | `/api/orders/{id}/status` | 是 | admin/operator | 审核/取消审核 |
| DELETE | `/api/orders` | 是 | admin/operator | 删除订单 |
| GET | `/api/orders/equipment-stats` | 是 | - | 设备订单统计 |
| GET | `/api/orders/audited-sales` | 是 | - | 近30天销售订单 |

### 客户/设备接口

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/customers` | 是 | 客户列表（支持搜索） |
| GET | `/api/equipment` | 是 | 设备列表（支持状态筛选） |

---

## 七、部署与运行

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

依赖服务：PostgreSQL（端口 5432）、Redis（端口 6379）需提前启动。

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器监听 `http://localhost:5173`，API 请求通过 Vite 代理转发至 `http://localhost:8080`。

### 测试账号

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | 123456 | admin | 全部权限 |
| user | 123456 | operator | 查看、创建订单 |
