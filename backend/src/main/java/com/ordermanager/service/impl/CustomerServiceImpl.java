package com.ordermanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordermanager.entity.Customer;
import com.ordermanager.mapper.CustomerMapper;
import com.ordermanager.service.CustomerService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 客户Service实现类
 *
 * 正常状态客户列表缓存在Redis中（6小时），避免频繁查询数据库。
 * 带关键词搜索时绕过缓存，直接查库以保证结果实时性。
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private static final String CACHE_KEY = "customers";
    private static final long CACHE_EXPIRE_HOURS = 6;

    private final CustomerMapper customerMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public CustomerServiceImpl(CustomerMapper customerMapper,
                               RedisTemplate<String, Object> redisTemplate) {
        this.customerMapper = customerMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取客户列表
     *
     * @param keyword 搜索关键字（匹配客户名称或联系电话），为null时走缓存
     * @return 正常状态客户列表
     */
    @Override
    public List<Customer> getCustomerList(String keyword) {
        // 无关键词时优先从Redis缓存获取
        @SuppressWarnings("unchecked")
        List<Customer> cachedList = (List<Customer>) redisTemplate.opsForValue().get(CACHE_KEY);

        if (cachedList != null && keyword == null) {
            return cachedList;
        }

        // 查询数据库（仅返回正常客户）
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getStatus, "normal");

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 模糊匹配客户名称 OR 联系电话
            wrapper.and(w -> w
                    .like(Customer::getCustomerName, keyword)
                    .or()
                    .like(Customer::getContactPhone, keyword)
            );
        }

        List<Customer> customers = customerMapper.selectList(wrapper);

        // 无关键词时缓存查询结果
        if (keyword == null && !customers.isEmpty()) {
            redisTemplate.opsForValue().set(CACHE_KEY, customers, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return customers;
    }
}
