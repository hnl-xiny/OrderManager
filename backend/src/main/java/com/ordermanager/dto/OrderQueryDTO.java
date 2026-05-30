package com.ordermanager.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 订单查询参数DTO
 */
@Data
public class OrderQueryDTO {
    /** 页码（从1开始） */
    private Integer page = 1;
    /** 每页条数 */
    private Integer pageSize = 8;
    /** 订单类型过滤 */
    private String orderType;
    /** 订单状态过滤 */
    private String orderStatus;
    /** 创建日期范围起（闭区间） */
    private LocalDate startDate;
    /** 创建日期范围止（闭区间） */
    private LocalDate endDate;
    /** 关键字搜索（匹配订单编号/客户名/设备名） */
    private String keyword;
}
