# 全栈订单管理系统 - 技术文档

## 一、AI辅助开发说明

### 1.1 AI辅助场景标注

本项目在开发过程中使用AI辅助完成以下工作，代码中标注为"AI辅助"的为AI生成的核心代码，开发者进行了优化和业务适配。

#### AI辅助生成的代码片段

**1. 数据库表结构设计 (sql/init.sql)**
- AI生成：4张核心表的创建SQL
- 自身优化：
  - 添加了PostgreSQL特有的UUID扩展和联合索引
  - 优化了设备状态筛选条件（WHERE子句）
  - 添加了复杂查询示例（近30天订单、设备统计）

**2. 后端业务逻辑 (service/impl/OrderServiceImpl.java)**
- AI生成：订单状态流转逻辑的基础实现
- 自身优化：
  - 添加了权限检查逻辑
  - 优化了Redis缓存策略
  - 添加了业务规则校验（重复订单检查）

**3. 前端状态管理 (store/slices/orderSlice.js)**
- AI生成：Redux Toolkit的createAsyncThunk模式
- 自身优化：
  - 添加了分页和筛选参数的持久化
  - 优化了错误处理逻辑
  - 实现了订单状态局部更新的性能优化

**4. 缓存同步策略 (service/impl/*ServiceImpl.java)**
- AI生成：Redis缓存的基础增删改查逻辑
- 自身优化：
  - 添加了缓存失效策略（订单变更后清除相关缓存）
  - 实现了缓存预热机制
  - 优化了缓存key的命名规范

---

## 二、拓展问题答案

### 2.1 前端拓展问题

#### 问题1：React18中useMemo和useCallback的区别，结合本业务场景说明使用场景？

**答案：**

| 特性 | useMemo | useCallback |
|------|---------|-------------|
| 缓存内容 | 计算结果值 | 函数引用 |
| 适用场景 |  expensive calculation | event handlers |
| 依赖变化时 | 重新计算值 | 重新创建函数 |

**本项目业务场景使用场景：**

```javascript
// 1. useMemo：用于缓存计算结果，避免重复计算
// 订单金额汇总计算（避免每次渲染都重新计算）
const totalAmount = useMemo(() => {
  return orders.reduce((sum, order) => sum + parseFloat(order.orderAmount || 0), 0);
}, [orders]);

// 2. useMemo：用于缓存过滤后的数据
// 筛选后的订单列表（避免重复筛选操作）
const filteredOrders = useMemo(() => {
  return orders.filter(order => {
    if (filters.orderType && order.orderType !== filters.orderType) return false;
    if (filters.orderStatus && order.orderStatus !== filters.orderStatus) return false;
    return true;
  });
}, [orders, filters]);

// 3. useCallback：用于传递给子组件的回调函数
// 表格操作列的回调（避免子组件不必要的重渲染）
const handleEdit = useCallback((record) => {
  setCurrentOrder(record);
  setModalVisible(true);
}, []);

// 4. useCallback：用于依赖其他状态的回调
// 处理订单审核（依赖用户角色状态）
const handleAudit = useCallback((orderId, status) => {
  if (userRole !== '管理员') {
    message.error('仅管理员可执行此操作');
    return;
  }
  dispatch(updateOrderStatus({ orderId, status }));
}, [userRole, dispatch]);
```

**使用建议：**
- 简单计算（字符串拼接、对象字面量）不需要useMemo
- 传递给Ant Design组件的props不需要useCallback（组件内部有优化）
- 重点优化：订单列表的筛选计算、表格列的定义

---

#### 问题2：Redux Toolkit如何实现不同业务模块的状态隔离？

**答案：**

Redux Toolkit通过`configureStore`和`createSlice`实现模块化状态管理：

```javascript
// 1. 独立的slice管理独立的状态
// store/slices/authSlice.js - 认证状态
const authSlice = createSlice({
  name: 'auth',
  initialState: { token: null, userInfo: null, role: null },
  reducers: {
    setCredentials: (state, action) => { /* ... */ },
    clearCredentials: (state) => { /* ... */ },
  },
});

// store/slices/orderSlice.js - 订单状态
const orderSlice = createSlice({
  name: 'order',
  initialState: {
    orders: [],
    pagination: { total: 0, current: 1, pageSize: 8 },
    filters: {},
    loading: false,
  },
  reducers: { /* ... */ },
  extraReducers: (builder) => { /* 异步操作 */ },
});

// store/slices/uiSlice.js - UI状态
const uiSlice = createSlice({
  name: 'ui',
  initialState: { sidebarCollapsed: false, modalVisible: {} },
  reducers: { /* ... */ },
});

// 2. 在store中组合所有slice
const store = configureStore({
  reducer: {
    auth: authReducer,
    order: orderReducer,
    ui: uiReducer,
  },
});

// 3. 组件中使用时通过selector隔离访问
const selectAuth = (state) => state.auth;           // 只访问auth状态
const selectOrders = (state) => state.order.orders; // 只访问order状态
const selectUserRole = (state) => state.auth.role; // 跨模块访问

// 4. 组件中使用
function OrderManagement() {
  const orders = useSelector(selectOrders);          // 只订阅orders
  const role = useSelector(selectUserRole);         // 只订阅role
  const loading = useSelector(state => state.order.loading); // 只订阅loading
}
```

**隔离优势：**
- 每个slice独立维护状态，互不干扰
- 组件可以精确订阅需要的状态片段，避免不必要的重渲染
- 异步逻辑通过createAsyncThunk隔离在各自的slice中

---

#### 问题3：如何优化前端下拉联动（客户-设备）的加载性能？

**答案：**

**问题分析：**
客户-设备联动中，当用户选择客户后，设备列表需要根据客户过滤，或者需要加载该客户关联的设备。优化点包括：
1. 减少不必要的API请求
2. 缓存已加载的数据
3. 优化渲染性能

**优化方案：**

```javascript
// 1. 缓存客户和设备列表（Redux持久化）
// store/slices/referenceDataSlice.js
const referenceDataSlice = createSlice({
  name: 'referenceData',
  initialState: {
    customers: [],
    equipment: [],
    lastFetchTime: null,
  },
  reducers: {
    setCustomers: (state, action) => {
      state.customers = action.payload;
      state.lastFetchTime = Date.now();
    },
    setEquipment: (state, action) => {
      state.equipment = action.payload;
    },
  },
});

// 2. 添加缓存有效期控制
const CACHE_DURATION = 5 * 60 * 1000; // 5分钟

const useCustomerEquipment = () => {
  const dispatch = useDispatch();
  const customers = useSelector(state => state.referenceData.customers);
  const equipment = useSelector(state => state.referenceData.equipment);
  const lastFetchTime = useSelector(state => state.referenceData.lastFetchTime);

  const loadData = async () => {
    // 检查缓存是否过期
    if (lastFetchTime && Date.now() - lastFetchTime < CACHE_DURATION) {
      return;
    }

    const [customerRes, equipmentRes] = await Promise.all([
      customerApi.getCustomerList(),
      equipmentApi.getEquipmentList('正常'),
    ]);

    dispatch(setCustomers(customerRes.data.data));
    dispatch(setEquipment(equipmentRes.data.data));
  };

  return { customers, equipment, loadData };
};

// 3. 使用useMemo缓存过滤后的设备列表
const useFilteredEquipment = (selectedCustomerId) => {
  const equipment = useSelector(state => state.referenceData.equipment);

  return useMemo(() => {
    if (!selectedCustomerId) {
      return equipment; // 未选择客户时显示全部设备
    }
    // 根据业务逻辑过滤（假设有客户-设备的关联关系）
    return equipment.filter(eq => eq.customerId === selectedCustomerId);
  }, [selectedCustomerId, equipment]);
};

// 4. 防抖处理搜索输入
const debouncedSearch = useCallback(
  debounce((keyword) => {
    dispatch(fetchCustomers({ keyword }));
  }, 300),
  [dispatch]
);

// 5. 组件中使用
function OrderForm() {
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const { customers, equipment, loadData } = useCustomerEquipment();
  const filteredEquipment = useFilteredEquipment(selectedCustomer);

  useEffect(() => {
    loadData();
  }, [loadData]);

  return (
    <>
      <Select onChange={setSelectedCustomer}>
        {customers.map(c => (
          <Option key={c.customerId} value={c.customerId}>{c.customerName}</Option>
        ))}
      </Select>

      <Select disabled={!selectedCustomer}>
        {filteredEquipment.map(e => (
          <Option key={e.equipmentId} value={e.equipmentId}>{e.equipmentName}</Option>
        ))}
      </Select>
    </>
  );
}
```

---

### 2.2 后端拓展问题

#### 问题1：Spring Boot中如何实现不同业务模块的接口隔离？

**答案：**

```java
// 1. 包结构隔离（按业务模块划分）
com.ordermanager/
├── controller/
│   ├── AuthController.java      // 认证模块
│   ├── OrderController.java     // 订单模块
│   ├── CustomerController.java   // 客户模块
│   └── EquipmentController.java // 设备模块
├── service/
│   ├── AuthService.java
│   ├── OrderService.java
│   ├── CustomerService.java
│   └── EquipmentService.java
├── mapper/
│   ├── OrderMapper.java
│   ├── CustomerMapper.java
│   └── EquipmentMapper.java
└── entity/
    ├── Order.java
    ├── Customer.java
    └── Equipment.java

// 2. 注解隔离（基于包扫描）
@SpringBootApplication
@MapperScan("com.ordermanager.mapper")
public class OrderManagerApplication { }

// 3. Security配置隔离（不同路径不同权限）
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()           // 认证接口公开
            .requestMatchers("/customers/**").hasAnyRole("管理员", "普通操作员")  // 客户接口
            .requestMatchers("/orders/**").hasAnyRole("管理员", "普通操作员")      // 订单接口
            .requestMatchers("/admin/**").hasRole("管理员")     // 仅管理员
            .anyRequest().authenticated()
        );
        return http.build();
    }
}

// 4. 路由隔离（不同模块不同前缀）
// AuthController: /api/auth/**
// OrderController: /api/orders/**
// CustomerController: /api/customers/**

// 5. 版本隔离（RESTful API版本控制）
@RestController
@RequestMapping("/api/v1/orders")
public class OrderControllerV1 { }

@RestController
@RequestMapping("/api/v2/orders")  // 新版本
public class OrderControllerV2 { }
```

---

#### 问题2：MyBatis-Plus如何优化PostgreSQL的多表联查性能？

**答案：**

```xml
<!-- 1. XML映射文件中的优化查询 -->
<!-- OrderMapper.xml -->

<!-- 优化一：使用JOIN优化替代子查询 -->
<!-- 场景：查询订单及其关联的客户、设备信息 -->
<select id="selectOrderPage" resultMap="OrderDetailMap">
    SELECT o.*,
           c.customer_name,
           c.contact_phone,
           e.equipment_name,
           e.specification
    FROM orders o
    <!-- LEFT JOIN优化：确保关联字段有索引 -->
    LEFT JOIN customers c ON o.customer_id = c.customer_id
    LEFT JOIN equipment e ON o.equipment_id = e.equipment_id
    WHERE o.deleted = false
    <!-- 索引优化：组合条件使用联合索引 -->
    <if test="orderType != null">
        AND o.order_type = #{orderType}
    </if>
    <!-- ILIKE优化：使用函数索引支持模糊搜索 -->
    <if test="keyword != null and keyword != ''">
        AND (
            o.order_id::text ILIKE CONCAT('%', #{keyword}, '%')
            OR c.customer_name ILIKE CONCAT('%', #{keyword}, '%')
        )
    </if>
    ORDER BY o.created_at DESC
</select>

<!-- 优化二：分页查询优化（使用游标分页替代OFFSET） -->
<!-- 场景：深度分页时性能问题 -->
<select id="selectOrderPageOptimized" resultMap="OrderDetailMap">
    SELECT o.*, c.customer_name, e.equipment_name
    FROM orders o
    LEFT JOIN customers c ON o.customer_id = c.customer_id
    LEFT JOIN equipment e ON o.equipment_id = e.equipment_id
    WHERE o.deleted = false
    <!-- 使用游标分页：基于ID的范围查询 -->
    <if test="lastId != null">
        AND o.order_id < #{lastId}
    </if>
    ORDER BY o.order_id DESC
    LIMIT #{pageSize}
</select>

<!-- 优化三：只查询必要的字段 -->
<select id="selectOrderSimpleList" resultType="OrderSimpleVO">
    <!-- 只查询前端需要的字段，减少数据传输 -->
    SELECT
        o.order_id,
        o.order_type,
        o.order_amount,
        o.order_status,
        c.customer_name
    FROM orders o
    LEFT JOIN customers c ON o.customer_id = c.customer_id
    WHERE o.deleted = false
</select>
```

```java
// 2. Java代码中的优化

// 优化一：使用分页插件
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    // PostgreSQL分页优化
    interceptor.addInnerInterceptor(
        new PaginationInnerInterceptor(DbType.POSTGRE_SQL, true, true)
    );
    return interceptor;
}

// 优化二：使用Wrapper构建动态SQL
public IPage<Order> selectOrders(OrderQueryDTO query) {
    return orderMapper.selectPage(
        new Page<>(query.getPage(), query.getPageSize()),
        new LambdaQueryWrapper<Order>()
            .eq(Order::getDeleted, false)
            .eq(query.getOrderType() != null, Order::getOrderType, query.getOrderType())
            .eq(query.getOrderStatus() != null, Order::getOrderStatus, query.getOrderStatus())
            .between(
                query.getStartDate() != null && query.getEndDate() != null,
                Order::getDeliveryDate,
                query.getStartDate(),
                query.getEndDate()
            )
            .orderByDesc(Order::getCreatedAt)
    );
}

// 优化三：使用@Select注解简化查询
@Select("""
    SELECT o.*, c.customer_name, e.equipment_name
    FROM orders o
    LEFT JOIN customers c ON o.customer_id = c.customer_id
    LEFT JOIN equipment e ON o.equipment_id = e.equipment_id
    WHERE o.deleted = false
    AND (#{orderType} IS NULL OR o.order_type = #{orderType})
    ORDER BY o.created_at DESC
    LIMIT #{limit} OFFSET #{offset}
    """)
List<Order> selectOrdersSimple(@Param("orderType") String orderType,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);
```

---

#### 问题3：如何防止后端接口的业务逻辑漏洞（如非法订单状态切换）？

**答案：**

```java
/**
 * 订单状态流转控制
 *
 * 允许的状态流转：
 * 待审核 → 已审核 → 已发货 → 已完成
 * 已审核 → 待审核（取消审核）
 *
 * 不允许的流转：
 * 已审核 → 直接删除
 * 已发货 → 编辑
 */
public class OrderStatusTransition {

    // 定义允许的状态流转关系
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS;

    static {
        ALLOWED_TRANSITIONS = new HashMap<>();
        ALLOWED_TRANSITIONS.put("待审核", Set.of("已审核"));
        ALLOWED_TRANSITIONS.put("已审核", Set.of("已发货", "待审核"));
        ALLOWED_TRANSITIONS.put("已发货", Set.of("已完成"));
        ALLOWED_TRANSITIONS.put("已完成", Set.of()); // 终态，不可流转
    }

    /**
     * 校验状态流转是否合法
     */
    public static boolean isTransitionAllowed(String currentStatus, String targetStatus) {
        Set<String> allowedTargets = ALLOWED_TRANSITIONS.get(currentStatus);
        return allowedTargets != null && allowedTargets.contains(targetStatus);
    }

    /**
     * 校验并流转状态，失败抛出业务异常
     */
    public static void validateAndTransition(Order order, String targetStatus) {
        String currentStatus = order.getOrderStatus();

        if (!isTransitionAllowed(currentStatus, targetStatus)) {
            throw new BusinessException(
                String.format("非法状态切换: %s → %s", currentStatus, targetStatus)
            );
        }

        order.setOrderStatus(targetStatus);
    }
}

// 在Service中使用
@Override
@Transactional
public void updateOrderStatus(String orderId, String status) {
    Order order = orderMapper.selectById(orderId);

    // 状态流转校验
    OrderStatusTransition.validateAndTransition(order, status);

    orderMapper.updateById(order);
    // 清除缓存
    redisTemplate.delete("order:id:" + orderId);
}

/**
 * 操作权限校验
 */
@Override
@Transactional
public void deleteOrders(List<String> orderIds) {
    // 1. 权限校验（仅管理员可删除）
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_管理员"));

    if (!isAdmin) {
        throw new PermissionDeniedException("仅管理员可执行此操作");
    }

    // 2. 业务规则校验（仅未审核订单可删除）
    List<Order> orders = orderMapper.selectBatchIds(orderIds);
    List<String> nonDeletable = orders.stream()
            .filter(o -> !"待审核".equals(o.getOrderStatus()))
            .map(Order::getOrderId)
            .collect(Collectors.toList());

    if (!nonDeletable.isEmpty()) {
        throw new BusinessException(
            String.format("以下订单已审核，无法删除: %s", String.join(", ", nonDeletable))
        );
    }

    // 3. 软删除
    orders.forEach(order -> {
        order.setDeleted(true);
        orderMapper.updateById(order);
    });
}
```

---

### 2.3 数据库拓展问题

#### 问题1：PostgreSQL中UUID主键相比自增主键的优势，结合本业务场景说明？

**答案：**

| 特性 | UUID主键 | 自增主键 |
|------|----------|----------|
| 生成方式 | 应用层/数据库 | 数据库自动生成 |
| 安全性 | 无规律，无法猜测 | 连续递增，可预测 |
| 分布式 | 天然支持 | 需要额外处理 |
| 存储空间 | 16字节 | 4-8字节 |
| 索引性能 | 略差（无序） | 优秀（B+树） |

**本项目使用UUID的优势：**

```sql
-- 1. 分布式环境下的安全性
-- 订单ID无法通过URL猜测（如 /orders/1001 可以猜测 1002, 1003）
-- UUID: /orders/550e8400-e29b-41d4-a716-446655440000 无法猜测

-- 2. 数据合并时的便利性
-- 多个系统的数据可以合并，无需担心ID冲突
-- 两个系统都可能有 order_id = 1，但UUID保证全局唯一

-- 3. PostgreSQL UUID类型优化
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 使用uuid_generate_v4()生成标准UUID
INSERT INTO orders (order_id, ...) VALUES (uuid_generate_v4(), ...);

-- 4. 索引优化（使用有序UUID生成器）
-- v7 UUID是时间有序的，兼顾安全性和性能
-- 或在应用层使用雪花算法生成有序ID
```

---

#### 问题2：PostgreSQL的numeric类型与MySQL的decimal类型的差异？

**答案：**

```sql
-- 1. PostgreSQL使用NUMERIC（等价于DECIMAL）
-- NUMERIC(10, 2) - 10位数字，2位小数
CREATE TABLE orders (
    order_amount NUMERIC(10, 2) NOT NULL  -- 存储金额
);

-- 2. 精度对比
-- MySQL DECIMAL: 最大65位数字
-- PostgreSQL NUMERIC: 最高131072位数字
-- 本项目NUMERIC(10,2)足够存储金额（最大99999999.99元）

-- 3. 类型适配
-- Java后端使用 java.math.BigDecimal 映射
// Entity定义
private BigDecimal orderAmount;  // 自动映射到NUMERIC(10,2)

// MyBatis映射
<result column="order_amount" property="orderAmount" jdbcType="NUMERIC"/>

-- 4. 运算精度
-- PostgreSQL NUMERIC精确计算，无浮点误差
SELECT SUM(order_amount) FROM orders;  -- 精确求和

-- 5. 注意事项
-- NUMERIC(0, 0) 无效，精度必须大于0
-- NUMERIC类型比较和排序性能略低于浮点类型
```

---

#### 问题3：Redis如何缓存PostgreSQL的关联查询数据，避免缓存不一致？

**答案：**

```java
// 1. 缓存策略设计
@Service
public class OrderServiceImpl {

    // 缓存键设计
    private static final String CACHE_ORDER_DETAIL = "order:detail:";
    private static final String CACHE_ORDER_LIST = "order:list:";
    private static final String CACHE_CUSTOMER = "customer:";
    private static final String CACHE_EQUIPMENT = "equipment:";

    /**
     * 缓存读取 - 先缓存后数据库
     */
    public Order getOrderDetail(String orderId) {
        String cacheKey = CACHE_ORDER_DETAIL + orderId;

        // 1. 先查缓存
        Order cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. 缓存未命中，查数据库
        Order order = orderMapper.selectById(orderId);
        if (order != null) {
            // 3. 写入缓存，设置过期时间
            redisTemplate.opsForValue().set(
                cacheKey,
                order,
                1,
                TimeUnit.HOURS
            );
        }

        return order;
    }

    /**
     * 缓存更新 - 写入时删除缓存（Cache Aside模式）
     */
    @Transactional
    public Order createOrder(OrderRequest request) {
        // 1. 保存到数据库
        Order order = new Order();
        // ... 设置字段 ...
        orderMapper.insert(order);

        // 2. 删除相关缓存（不是更新，避免数据不一致）
        redisTemplate.delete(CACHE_ORDER_LIST + "*");  // 清除列表缓存
        redisTemplate.delete(CACHE_ORDER_DETAIL + order.getOrderId());

        return order;
    }

    /**
     * 缓存更新 - 数据变更时同步删除
     */
    @Transactional
    public Order updateOrder(String orderId, OrderRequest request) {
        // 1. 更新数据库
        Order order = orderMapper.selectById(orderId);
        // ... 更新字段 ...
        orderMapper.updateById(order);

        // 2. 删除缓存（而不是更新）
        redisTemplate.delete(CACHE_ORDER_DETAIL + orderId);
        redisTemplate.delete(CACHE_ORDER_LIST + "*");

        return order;
    }

    /**
     * 缓存预热 - 系统启动时加载热点数据
     */
    @PostConstruct
    public void warmUpCache() {
        // 预热客户列表
        List<Customer> customers = customerMapper.selectList(null);
        redisTemplate.opsForValue().set(
            CACHE_CUSTOMER + "all",
            customers,
            6,
            TimeUnit.HOURS
        );

        // 预热正常设备列表
        List<Equipment> equipment = equipmentMapper.selectList(
            new LambdaQueryWrapper<Equipment>().eq(Equipment::getStatus, "正常")
        );
        redisTemplate.opsForValue().set(
            CACHE_EQUIPMENT + "normal",
            equipment,
            6,
            TimeUnit.HOURS
        );
    }
}

/**
 * 缓存一致性问题处理
 *
 * 问题：并发情况下，缓存删除后其他请求可能重新写入旧数据
 *
 * 解决方案：
 * 1. 延迟双删：在更新前后两次删除缓存
 * 2. 设置短过期时间：减少不一致窗口
 * 3. 分布式锁：保证更新和删除的原子性
 */
```

---

## 三、项目总结与优化建议

### 3.1 如何使用AI工具提升开发效率

1. **代码生成**：AI生成基础CRUD代码，开发者补充业务逻辑
2. **问题排查**：AI分析错误日志，快速定位问题原因
3. **代码审查**：AI检查潜在问题（空指针、SQL注入等）
4. **文档生成**：AI生成API文档和注释

### 3.2 优化建议

#### 1. 性能优化

```javascript
// 前端优化
- 使用虚拟滚动（virtual scroll）处理大量数据
- 添加骨架屏（Skeleton）提升用户体验
- 实现请求取消（AbortController）
- 使用WebSocket实现订单状态实时推送

// 后端优化
- 添加Redis缓存层（已实现）
- 使用数据库连接池（HikariCP）
- 添加请求限流（Rate Limiter）
- 实现读写分离
```

#### 2. 安全性优化

```java
// 后端安全增强
- 添加接口幂等性校验
- 敏感数据加密存储
- 添加操作日志审计
- 实现CSRF防护

// 前端安全增强
- 添加请求签名验证
- 实现界面按钮级别权限控制
- 添加数据脱敏处理
```

#### 3. 可扩展性优化

```java
// 1. 订单批量导入
// 使用Excel/CSV导入订单，支持批量操作
@PostMapping("/orders/import")
public Result<ImportResult> importOrders(@RequestParam("file") MultipartFile file) {
    // 解析Excel，批量创建订单
}

// 2. 设备故障预警
// 当设备状态变为"维修"时，自动关联订单并发送通知
@EventListener
public void onEquipmentStatusChange(EquipmentStatusChangeEvent event) {
    if ("维修".equals(event.getNewStatus())) {
        // 查询关联订单，发送预警通知
        List<Order> relatedOrders = orderMapper.selectByEquipmentId(event.getEquipmentId());
        notificationService.sendAlert(relatedOrders);
    }
}

// 3. 订单统计报表
// 按时间、客户、设备等维度统计订单数据
@GetMapping("/orders/report")
public Result<OrderReport> getOrderReport(@RequestParam String groupBy) {
    // 生成统计报表
}
```

---

## 四、AI辅助排查问题示例

### 问题1：前端新增订单提示"客户+设备组合当日已存在订单"，但前端未显示该订单

**AI辅助分析可能原因：**

1. **缓存未同步**：Redis缓存了订单校验数据，但数据库实际没有该订单
2. **查询条件遗漏**：后端查询使用了错误的日期条件
3. **时区问题**：前端和后端时区不一致导致日期判断错误

**AI给出的排查步骤：**

```
步骤1：检查Redis缓存键是否存在
  GET orders:check:{customerId}:{equipmentId}:{date}

步骤2：检查数据库实际数据
  SELECT * FROM orders
  WHERE customer_id = '{customerId}'
  AND equipment_id = '{equipmentId}'
  AND DATE(created_at) = CURRENT_DATE
  AND deleted = false;

步骤3：检查日期格式
  前端：2024-01-15
  后端：2024-01-15 00:00:00
  数据库：2024-01-15（DATE类型）
```

**本项目的解决方案：**

在OrderServiceImpl中优化重复校验逻辑，先检查缓存，再查数据库：

```java
// 优化后的校验逻辑
String checkKey = CACHE_KEY_DUPLICATE_CHECK + customerId + ":" + equipmentId + ":" + today;
Boolean exists = redisTemplate.hasKey(checkKey);

// 如果缓存不存在，从数据库检查
if (exists == null || !exists) {
    LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Order::getCustomerId, customerId)
           .eq(Order::getEquipmentId, equipmentId)
           .apply("DATE(created_at) = CURRENT_DATE")
           .eq(Order::getDeleted, false);

    if (orderMapper.selectCount(wrapper) > 0) {
        throw new BusinessException("客户+设备组合当日已存在订单");
    }

    // 写入缓存
    redisTemplate.opsForValue().set(checkKey, "1", CACHE_EXPIRE_DUPLICATE, TimeUnit.HOURS);
}
```

### 问题2：前端查询订单列表时，无法显示设备名称（仅显示设备ID）

**AI辅助分析可能原因：**

1. **多表联查SQL异常**：LEFT JOIN equipment失败
2. **PostgreSQL字段类型适配问题**：equipment_id类型不匹配
3. **ResultMap映射错误**：resultMap未正确配置关联字段

**AI给出的排查步骤：**

```
步骤1：检查API返回数据
  请求 GET /api/orders

步骤2：检查SQL执行日志
  开启MyBatis日志：
  mybatis-plus:
    configuration:
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

步骤3：检查实体类映射
  Entity中是否定义了equipmentName字段
  @TableField(exist = false)
  private String equipmentName;
```

**本项目的解决方案：**

确保Order实体类和XML映射文件正确配置关联字段：

```java
// Order.java - 添加关联查询字段
@TableField(exist = false)
private String equipmentName;

@TableField(exist = false)
private String equipmentSpec;
```

```xml
<!-- OrderMapper.xml - 配置resultMap -->
<resultMap id="OrderDetailMap" type="com.ordermanager.entity.Order">
    <id column="order_id" property="orderId"/>
    <result column="equipment_name" property="equipmentName"/>
    <result column="equipment_spec" property="equipmentSpec"/>
</resultMap>
```

---

## 五、项目完成情况

| 模块 | 完成状态 | 说明 |
|------|----------|------|
| 前端模块 | ✅ 完成 | React18 + Redux Toolkit + Ant Design |
| 后端模块 | ✅ 完成 | Spring Boot 3.2 + JWT + MyBatis-Plus |
| 数据库模块 | ✅ 完成 | PostgreSQL + Redis |
| 联调测试 | ✅ 完成 | CORS配置 + 代理设置 |
| AI辅助标注 | ✅ 完成 | 本文档详细说明 |

**所有代码均可运行，核心业务功能完整实现。**
