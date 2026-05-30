package com.ordermanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ordermanager.handler.UUIDTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 客户实体类
 * 对应表: customers
 */
@Data
@TableName(value = "customers", autoResultMap = true)
public class Customer {
    /** 客户唯一标识，插入时自动分配UUID */
    @TableId(value = "customer_id", type = IdType.ASSIGN_UUID)
    @TableField(value = "customer_id", typeHandler = UUIDTypeHandler.class)
    private UUID customerId;

    /** 客户名称 */
    @TableField("customer_name")
    private String customerName;

    /** 联系人姓名 */
    @TableField("contact_person")
    private String contactPerson;

    /** 联系电话 */
    @TableField("contact_phone")
    private String contactPhone;

    /** 状态：normal-正常 / disabled-已禁用 */
    @TableField("status")
    private String status;

    /** 记录创建时间，插入时自动填充 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 最近一次更新时间，更新时自动填充 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
