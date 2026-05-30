package com.ordermanager.service;

import com.ordermanager.entity.Equipment;

import java.util.List;

/**
 * 设备Service接口
 */
public interface EquipmentService {
    /**
     * 获取设备列表（可筛选正常可用设备）
     */
    List<Equipment> getEquipmentList(String status);
}
