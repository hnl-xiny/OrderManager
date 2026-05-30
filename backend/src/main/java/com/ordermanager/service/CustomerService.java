package com.ordermanager.service;

import com.ordermanager.entity.Customer;

import java.util.List;

/**
 * 客户Service接口
 */
public interface CustomerService {
    /**
     * 获取客户列表（支持模糊搜索）
     */
    List<Customer> getCustomerList(String keyword);
}
