package com.ordermanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordermanager.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户Mapper
 *
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得 CRUD 基础操作（selectById/insert/update/deleteById 等）。
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
