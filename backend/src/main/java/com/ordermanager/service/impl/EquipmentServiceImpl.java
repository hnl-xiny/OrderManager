package com.ordermanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordermanager.entity.Equipment;
import com.ordermanager.mapper.EquipmentMapper;
import com.ordermanager.service.EquipmentService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 设备Service实现类
 *
 * 正常状态设备列表缓存在Redis中（6小时），筛选非normal状态时绕过缓存。
 */
@Service
public class EquipmentServiceImpl implements EquipmentService {

    private static final String CACHE_KEY_NORMAL = "equipment:normal";
    private static final long CACHE_EXPIRE_HOURS = 6;

    private final EquipmentMapper equipmentMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public EquipmentServiceImpl(EquipmentMapper equipmentMapper,
                               RedisTemplate<String, Object> redisTemplate) {
        this.equipmentMapper = equipmentMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取设备列表
     *
     * @param status 设备状态过滤（normal/repair/disabled），为null时返回全部
     * @return 符合条件的设备列表
     */
    @Override
    public List<Equipment> getEquipmentList(String status) {
        // 仅筛选normal状态时使用缓存
        if ("normal".equals(status)) {
            @SuppressWarnings("unchecked")
            List<Equipment> cachedList = (List<Equipment>) redisTemplate.opsForValue().get(CACHE_KEY_NORMAL);

            if (cachedList != null) {
                return cachedList;
            }
        }

        // 查询数据库
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Equipment::getStatus, status);
        }

        List<Equipment> equipmentList = equipmentMapper.selectList(wrapper);

        // 缓存normal状态设备列表
        if ("normal".equals(status) && !equipmentList.isEmpty()) {
            redisTemplate.opsForValue().set(CACHE_KEY_NORMAL, equipmentList, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return equipmentList;
    }
}
