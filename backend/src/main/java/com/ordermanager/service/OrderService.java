package com.ordermanager.service;

import com.ordermanager.dto.OrderQueryDTO;
import com.ordermanager.dto.OrderRequest;
import com.ordermanager.entity.Order;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单服务接口
 */
public interface OrderService {
    /**
     * 分页查询订单列表
     */
    Map<String, Object> getOrderList(OrderQueryDTO queryDTO);

    /**
     * 获取订单详情
     */
    Order getOrderDetail(String orderId);

    /**
     * 新增订单
     */
    Order createOrder(OrderRequest request, String username, UUID userId);

    /**
     * 更新订单
     */
    Order updateOrder(String orderId, OrderRequest request);

    /**
     * 审核/取消审核订单
     */
    void updateOrderStatus(String orderId, String status, String username);

    /**
     * 删除订单（单个/批量）
     */
    void deleteOrders(List<String> orderIds);

    /**
     * 获取近30天已审核的销售订单
     */
    List<Order> getRecentAuditedSalesOrders();

    /**
     * 统计各设备订单数量
     */
    List<Map<String, Object>> countOrdersByEquipment();
}
