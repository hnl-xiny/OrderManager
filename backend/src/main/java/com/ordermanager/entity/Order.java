package com.ordermanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ordermanager.handler.UUIDTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 订单实体类
 * 对应表: orders
 */
@Data
@TableName(value = "orders", autoResultMap = true)
public class Order {
    // ========== 业务主键/外键 ==========
    /** 订单唯一标识（UUID），手动注入 */
    @TableId(value = "order_id", type = IdType.INPUT)
    @TableField(value = "order_id", typeHandler = UUIDTypeHandler.class)
    private UUID orderId;

    /** 关联客户ID */
    @TableField(value = "customer_id", typeHandler = UUIDTypeHandler.class)
    private UUID customerId;

    /** 关联设备ID */
    @TableField(value = "equipment_id", typeHandler = UUIDTypeHandler.class)
    private UUID equipmentId;

    // ========== 业务字段 ==========
    /** 订单类型：purchase-采购 / sales-销售 */
    @TableField("order_type")
    private String orderType;

    /** 订单金额 */
    @TableField("order_amount")
    private BigDecimal orderAmount;

    /** 计划交付日期 */
    @TableField("delivery_date")
    private LocalDate deliveryDate;

    /** 订单状态：pending-待审核 / approved-已通过 / shipped-已发货 / completed-已完成 */
    @TableField("order_status")
    private String orderStatus;

    /** 备注信息 */
    @TableField("remarks")
    private String remarks;

    // ========== 审计字段 ==========
    /** 创建人用户ID */
    @TableField(value = "created_by", typeHandler = UUIDTypeHandler.class)
    private UUID createdBy;

    /** 记录创建时间，插入时自动填充 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 最近一次更新时间，更新时自动填充 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 最近修改时间，记录订单的任何操作（审核/编辑/删除等）时间
     */
    @TableField("last_modified_at")
    private LocalDateTime lastModifiedAt;

    /** 逻辑删除标记：true=已删除 */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    // ========== 关联查询字段（非数据库列，由JOIN查询填充）==========
    /** 客户名称（关联查询结果，非持久化字段） */
    @TableField(exist = false)
    private String customerName;

    /** 客户联系电话 */
    @TableField(exist = false)
    private String customerPhone;

    /** 设备名称 */
    @TableField(exist = false)
    private String equipmentName;

    /** 设备规格型号 */
    @TableField(exist = false)
    private String equipmentSpec;

    /** 创建人用户名 */
    @TableField(exist = false)
    private String creatorName;

    /**
     * 标识本次操作是否覆盖了已有订单（避免重复创建同一客户+设备的多条记录）
     */
    @TableField(exist = false)
    private Boolean overwritten;
}
