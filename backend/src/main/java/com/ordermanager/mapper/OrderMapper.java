package com.ordermanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ordermanager.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单Mapper
 *
 * 定义关联查询和需要特殊处理的自定义SQL方法。
 * UUID 类型字段通过 resultMap / typeHandler 处理，避免 MyBatis-Plus 自动映射错误。
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 分页查询订单列表（JOIN 客户表、设备表，创建人表）
     */
    IPage<Order> selectOrderPage(Page<Order> page,
                                @Param("orderType") String orderType,
                                @Param("orderStatus") String orderStatus,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate,
                                @Param("keyword") String keyword);

    /**
     * 根据ID查询订单详情（关联查询客户、设备、创建人信息）
     */
    Order selectOrderDetailById(@Param("orderId") UUID orderId);

    /**
     * 根据ID查询订单（走 resultMap 处理 UUID 类型）
     */
    Order selectByIdMapped(@Param("orderId") UUID orderId);

    /**
     * 查询今日同客户同设备的活跃（未删除）订单
     */
    Order selectActiveDuplicate(@Param("customerId") UUID customerId, @Param("equipmentId") UUID equipmentId);

    /**
     * 根据ID列表批量查询（走 resultMap 处理 UUID 类型）
     */
    List<Order> selectByIdsMapped(@Param("orderIds") List<UUID> orderIds);

    /**
     * 自定义更新订单方法：使用内联 typeHandler 处理 UUID 类型，
     * 避免 MyBatis-Plus 自动回填导致的类型转换错误
     */
    int updateOrderById(@Param("order") Order order);

    /**
     * 统计各设备关联的订单数量（仅关联正常设备）
     */
    List<Map<String, Object>> countOrdersByEquipment();

    /**
     * 查询近30天已审核的销售订单（按金额倒序）
     */
    List<Order> selectRecentAuditedSalesOrders(@Param("days") Integer days);
}
