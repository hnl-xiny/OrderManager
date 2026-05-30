package com.ordermanager.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ordermanager.dto.OrderQueryDTO;
import com.ordermanager.dto.OrderRequest;
import com.ordermanager.entity.Customer;
import com.ordermanager.entity.Equipment;
import com.ordermanager.entity.Order;
import com.ordermanager.handler.GlobalExceptionHandler.BusinessException;
import com.ordermanager.handler.GlobalExceptionHandler.DuplicateOrderException;
import com.ordermanager.handler.GlobalExceptionHandler.PermissionDeniedException;
import com.ordermanager.mapper.CustomerMapper;
import com.ordermanager.mapper.EquipmentMapper;
import com.ordermanager.mapper.OrderMapper;
import com.ordermanager.service.OrderService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单Service实现类
 *
 * 核心逻辑：
 * <ul>
 * <li>列表/详情查询：Redis 二级缓存，订单列表缓存10分钟，详情缓存1小时</li>
 * <li>新建订单：自动检测同日同客户同设备订单，存在则覆盖更新而非新建</li>
 * <li>编辑/删除：仅限 pending 状态的订单，管理员/操作员有权限</li>
 * <li>状态审核：admin/operator 可将 pending ↔ approved 互转</li>
 * </ul>
 *
 * 每次写操作（增/改/删）后主动清除列表缓存，保留详情缓存自然过期。
 */
@Service
public class OrderServiceImpl implements OrderService {

    // ========== 缓存Key与过期时间 ==========
    private static final String CACHE_KEY_ORDER_DETAIL = "order:id:";
    private static final String CACHE_KEY_PAGE = "orders:page:";
    private static final String CACHE_KEY_DUPLICATE_CHECK = "orders:check:";

    private static final long CACHE_EXPIRE_DETAIL = 1; // 订单详情缓存：1小时
    private static final long CACHE_EXPIRE_PAGE = 10; // 列表缓存：10分钟
    private static final long CACHE_EXPIRE_DUPLICATE = 24; // 重复检查缓存：24小时（目前未启用）

    private final OrderMapper orderMapper;
    private final CustomerMapper customerMapper;
    private final EquipmentMapper equipmentMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public OrderServiceImpl(OrderMapper orderMapper,
            CustomerMapper customerMapper,
            EquipmentMapper equipmentMapper,
            RedisTemplate<String, Object> redisTemplate) {
        this.orderMapper = orderMapper;
        this.customerMapper = customerMapper;
        this.equipmentMapper = equipmentMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 分页查询订单列表（关联客户、设备、创建人信息）
     *
     * 缓存策略：缓存 key 包含所有查询参数，写操作后清除。
     */
    @Override
    public Map<String, Object> getOrderList(OrderQueryDTO queryDTO) {
        // 生成缓存key（所有筛选条件作为key的一部分）
        String cacheKey = CACHE_KEY_PAGE + queryDTO.getPage() + ":" + queryDTO.getPageSize()
                + ":" + queryDTO.getOrderType() + ":" + queryDTO.getOrderStatus()
                + ":" + queryDTO.getStartDate() + ":" + queryDTO.getEndDate()
                + ":" + queryDTO.getKeyword();

        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 执行关联分页查询（JOIN 客户、设备信息）
        Page<Order> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<Order> result = orderMapper.selectOrderPage(
                page,
                queryDTO.getOrderType(),
                queryDTO.getOrderStatus(),
                queryDTO.getStartDate(),
                queryDTO.getEndDate(),
                queryDTO.getKeyword());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("records", result.getRecords());
        resultMap.put("total", result.getTotal());
        resultMap.put("pages", result.getPages());
        resultMap.put("current", result.getCurrent());
        resultMap.put("size", result.getSize());

        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, resultMap, CACHE_EXPIRE_PAGE, TimeUnit.MINUTES);

        return resultMap;
    }

    /**
     * 查询订单详情（关联查询客户、设备、创建人信息）
     *
     * 缓存策略：缓存1小时，写操作后主动删除。
     */
    @Override
    public Order getOrderDetail(String orderId) {
        String cacheKey = CACHE_KEY_ORDER_DETAIL + orderId;
        @SuppressWarnings("unchecked")
        Order cached = (Order) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Order order = orderMapper.selectOrderDetailById(UUID.fromString(orderId));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, order, CACHE_EXPIRE_DETAIL, TimeUnit.HOURS);

        return order;
    }

    /**
     * 新建订单
     *
     * 业务规则：
     * <ul>
     * <li>客户、设备必须存在且状态为 normal</li>
     * <li>同日同客户同设备已存在 pending 订单时，更新该订单而非新建（返回 overwritten=true）</li>
     * </ul>
     *
     * @param username 当前登录用户名（写入 createdBy）
     * @param userId   当前登录用户UUID
     */
    @Override
    @Transactional
    public Order createOrder(OrderRequest request, String username, UUID userId) {
        // 验证客户存在且状态正常
        Customer customer = customerMapper.selectById(request.getCustomerId());
        if (customer == null || !"normal".equals(customer.getStatus())) {
            throw new BusinessException("客户不存在或已被禁用");
        }

        // 验证设备存在且状态正常
        Equipment equipment = equipmentMapper.selectById(request.getEquipmentId());
        if (equipment == null || !"normal".equals(equipment.getStatus())) {
            throw new BusinessException("设备不存在或不可用");
        }

        // 查询今日是否有相同客户和设备的活跃订单，存在则覆盖更新
        Order existingOrder = orderMapper.selectActiveDuplicate(
                request.getCustomerId(), request.getEquipmentId());

        if (existingOrder != null) {
            existingOrder.setOrderType(request.getOrderType());
            existingOrder.setOrderAmount(request.getOrderAmount());
            existingOrder.setDeliveryDate(request.getDeliveryDate());
            existingOrder.setRemarks(request.getRemarks());
            existingOrder.setOrderStatus("pending");
            existingOrder.setUpdatedAt(LocalDateTime.now());
            existingOrder.setLastModifiedAt(LocalDateTime.now());
            existingOrder.setOverwritten(true);
            orderMapper.updateOrderById(existingOrder);
            clearOrderCache();
            return getOrderDetail(existingOrder.getOrderId().toString());
        }

        // 创建新订单
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setEquipmentId(request.getEquipmentId());
        order.setOrderType(request.getOrderType());
        order.setOrderAmount(request.getOrderAmount());
        order.setDeliveryDate(request.getDeliveryDate());
        order.setOrderStatus("pending");
        order.setRemarks(request.getRemarks());
        order.setCreatedBy(userId);
        order.setLastModifiedAt(LocalDateTime.now());
        order.setDeleted(false);

        orderMapper.insert(order);

        // 清除列表缓存
        clearOrderCache();

        return getOrderDetail(order.getOrderId().toString());
    }

    /**
     * 更新订单
     *
     * 限制：仅 pending 状态的订单可编辑。
     */
    @Override
    @Transactional
    public Order updateOrder(String orderId, OrderRequest request) {
        Order existOrder = orderMapper.selectByIdMapped(UUID.fromString(orderId));
        if (existOrder == null) {
            throw new BusinessException("订单不存在");
        }

        // 仅未审核订单可编辑
        if (!"pending".equals(existOrder.getOrderStatus())) {
            throw new BusinessException("仅待审核状态的订单可编辑");
        }

        // 更新字段
        existOrder.setCustomerId(request.getCustomerId());
        existOrder.setEquipmentId(request.getEquipmentId());
        existOrder.setOrderType(request.getOrderType());
        existOrder.setOrderAmount(request.getOrderAmount());
        existOrder.setDeliveryDate(request.getDeliveryDate());
        existOrder.setRemarks(request.getRemarks());
        existOrder.setUpdatedAt(LocalDateTime.now());
        existOrder.setLastModifiedAt(LocalDateTime.now());

        orderMapper.updateOrderById(existOrder);

        // 清除相关缓存
        clearOrderCache();
        redisTemplate.delete(CACHE_KEY_ORDER_DETAIL + orderId);

        return getOrderDetail(orderId);
    }

    /**
     * 审核/取消审核订单
     *
     * 权限：仅 admin/operator 角色可操作。<br>
     * 状态流转：pending ↔ approved（互转）。
     */
    @Override
    @Transactional
    public void updateOrderStatus(String orderId, String status, String username) {
        // 权限检查：要求当前用户具有 admin 或 operator 角色
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin") || a.getAuthority().equals("ROLE_operator"));

        if (!isAdmin) {
            throw new PermissionDeniedException("权限不足");
        }

        Order order = orderMapper.selectByIdMapped(UUID.fromString(orderId));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证并执行状态流转
        String currentStatus = order.getOrderStatus();
        if ("pending".equals(currentStatus) && "approved".equals(status)) {
            order.setOrderStatus(status);
        } else if ("approved".equals(currentStatus) && "pending".equals(status)) {
            order.setOrderStatus(status);
        } else {
            throw new BusinessException("无效的状态转换: " + currentStatus + " -> " + status);
        }

        order.setUpdatedAt(LocalDateTime.now());
        order.setLastModifiedAt(LocalDateTime.now());
        orderMapper.updateOrderById(order);

        // 清除缓存
        clearOrderCache();
        redisTemplate.delete(CACHE_KEY_ORDER_DETAIL + orderId);
    }

    /**
     * 删除订单（单个/批量）
     *
     * 限制：仅 pending 状态的订单可删除。<br>
     * 实现：软删除（deleted=true）。
     */
    @Override
    @Transactional
    public void deleteOrders(List<String> orderIds) {
        // 权限检查
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin") || a.getAuthority().equals("ROLE_operator"));

        if (!isAdmin) {
            throw new PermissionDeniedException("权限不足");
        }

        // 检查是否全部为未审核订单
        List<UUID> uuids = orderIds.stream().map(UUID::fromString).collect(Collectors.toList());
        List<Order> orders = orderMapper.selectByIdsMapped(uuids);
        List<String> nonDeletable = orders.stream()
                .filter(o -> !"pending".equals(o.getOrderStatus()))
                .map(o -> o.getOrderId().toString())
                .collect(Collectors.toList());

        if (!nonDeletable.isEmpty()) {
            throw new BusinessException("无法删除 " + nonDeletable.size() + " 条已审核订单");
        }

        // 软删除
        orders.forEach(order -> {
            order.setDeleted(true);
            order.setUpdatedAt(LocalDateTime.now());
            order.setLastModifiedAt(LocalDateTime.now());
            orderMapper.updateOrderById(order);
        });

        // 清除缓存
        clearOrderCache();
        orderIds.forEach(id -> redisTemplate.delete(CACHE_KEY_ORDER_DETAIL + id));
    }

    @Override
    public List<Order> getRecentAuditedSalesOrders() {
        return orderMapper.selectRecentAuditedSalesOrders(30);
    }

    @Override
    public List<Map<String, Object>> countOrdersByEquipment() {
        return orderMapper.countOrdersByEquipment();
    }

    /**
     * 清除所有订单列表缓存（分页缓存）
     *
     * 写操作后调用，防止脏读。详情缓存单独清除或等待自然过期。
     */
    private void clearOrderCache() {
        var keys = redisTemplate.keys(CACHE_KEY_PAGE + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
