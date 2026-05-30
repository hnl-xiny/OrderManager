package com.ordermanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ordermanager.handler.UUIDTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 设备实体类
 * 对应表: equipment
 */
@Data
@TableName(value = "equipment", autoResultMap = true)
public class Equipment {
    /** 设备唯一标识，插入时自动分配UUID */
    @TableId(value = "equipment_id", type = IdType.ASSIGN_UUID)
    @TableField(value = "equipment_id", typeHandler = UUIDTypeHandler.class)
    private UUID equipmentId;

    /** 设备编号（如出厂序列号） */
    @TableField("equipment_code")
    private String equipmentCode;

    /** 设备名称 */
    @TableField("equipment_name")
    private String equipmentName;

    /** 规格型号 */
    @TableField("specification")
    private String specification;

    /** 状态：normal-正常 / repair-维修中 / disabled-已停用 */
    @TableField("status")
    private String status;

    /** 记录创建时间，插入时自动填充 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 最近一次更新时间，更新时自动填充 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
