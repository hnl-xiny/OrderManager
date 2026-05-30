package com.ordermanager.controller;

import com.ordermanager.dto.OrderQueryDTO;
import com.ordermanager.dto.OrderRequest;
import com.ordermanager.dto.Result;
import com.ordermanager.entity.Order;
import com.ordermanager.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 获取订单列表（分页、筛选、搜索）
     * GET /api/orders
     */
    @GetMapping
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword) {

        OrderQueryDTO queryDTO = new OrderQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setPageSize(pageSize);
        queryDTO.setOrderType(orderType);
        queryDTO.setOrderStatus(orderStatus);
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setKeyword(keyword);

        Map<String, Object> result = orderService.getOrderList(queryDTO);
        return Result.success(result);
    }

    /**
     * 获取订单详情
     * GET /api/orders/:id
     */
    @GetMapping("/{id}")
    public Result<Order> getOrderDetail(@PathVariable String id) {
        Order order = orderService.getOrderDetail(id);
        return Result.success(order);
    }

    /**
     * 新增订单
     * POST /api/orders
     */
    @PostMapping
    public Result<Order> createOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {
        Order order = orderService.createOrder(request, authentication.getName(),
                (UUID) authentication.getCredentials());
        String msg = Boolean.TRUE.equals(order.getOverwritten())
                // equals会对比所有内容，包括创建时间，所有这里的？永远不会被执行
                ? "今日已存在相同客户和设备的订单，test"
                : "订单创建成功";
        return Result.success(msg, order);
    }

    /**
     * 编辑订单
     * PUT /api/orders/:id
     */
    @PutMapping("/{id}")
    public Result<Order> updateOrder(
            @PathVariable String id,
            @Valid @RequestBody OrderRequest request) {
        Order order = orderService.updateOrder(id, request);
        return Result.success("订单更新成功", order);
    }

    /**
     * 审核/取消审核订单
     * PUT /api/orders/:id/status
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateOrderStatus(
            @PathVariable String id,
            @RequestParam String status,
            Authentication authentication) {
        orderService.updateOrderStatus(id, status, authentication.getName());
        return Result.success("状态更新成功", null);
    }

    /**
     * 删除订单（单个/批量）
     * DELETE /api/orders
     */
    @DeleteMapping
    public Result<Void> deleteOrders(@RequestBody List<String> orderIds) {
        orderService.deleteOrders(orderIds);
        return Result.success("删除成功", null);
    }

    /**
     * 获取近30天已审核的销售订单（统计用）
     * GET /api/orders/audited-sales
     */
    @GetMapping("/audited-sales")
    public Result<List<Order>> getRecentAuditedSalesOrders() {
        List<Order> orders = orderService.getRecentAuditedSalesOrders();
        return Result.success(orders);
    }

    /**
     * 统计各设备订单数量
     * GET /api/orders/equipment-stats
     */
    @GetMapping("/equipment-stats")
    public Result<List<Map<String, Object>>> countOrdersByEquipment() {
        try {
            List<Map<String, Object>> stats = orderService.countOrdersByEquipment();
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("统计接口异常: " + e.getMessage());
        }
    }
}
