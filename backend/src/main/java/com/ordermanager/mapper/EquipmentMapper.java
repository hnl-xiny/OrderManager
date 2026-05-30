package com.ordermanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordermanager.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备Mapper
 *
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得 CRUD 基础操作。
 */
@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
}
