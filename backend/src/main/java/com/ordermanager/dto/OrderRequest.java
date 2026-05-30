package com.ordermanager.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 订单创建/更新请求DTO
 */
@Data
public class OrderRequest {
    /** 关联客户ID */
    @NotNull(message = "客户ID不能为空")
    private UUID customerId;

    /** 关联设备ID */
    @NotNull(message = "设备ID不能为空")
    private UUID equipmentId;

    /** 订单类型：purchase-采购 / sales-销售 */
    @NotBlank(message = "订单类型不能为空")
    private String orderType;

    /** 订单金额（正数，最多保留两位小数） */
    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.00", message = "订单金额不能为负数")
    @Digits(integer = 8, fraction = 2, message = "订单金额保留2位小数")
    private BigDecimal orderAmount;

    /** 计划交付日期（不能早于今天） */
    @NotNull(message = "交货日期不能为空")
    @FutureOrPresent(message = "交货日期不能小于当前日期")
    private LocalDate deliveryDate;

    /** 备注信息 */
    private String remarks;

    /**
     * 是否强制创建重复订单（跳过重复检查）
     * 当今日已存在相同客户+设备订单时，前端可传此字段强制新建
     */
    private Boolean forceCreate = false;
}
