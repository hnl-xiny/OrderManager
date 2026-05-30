package com.ordermanager.controller;

import com.ordermanager.dto.Result;
import com.ordermanager.entity.Customer;
import com.ordermanager.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户控制器
 */
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * 获取客户列表（支持模糊搜索）
     * GET /api/customers
     */
    @GetMapping
    public Result<List<Customer>> getCustomerList(
            @RequestParam(required = false) String keyword) {
        List<Customer> customers = customerService.getCustomerList(keyword);
        return Result.success(customers);
    }
}
