package com.ordermanager.controller;

import com.ordermanager.dto.Result;
import com.ordermanager.entity.Equipment;
import com.ordermanager.service.EquipmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备控制器
 */
@RestController
@RequestMapping("/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    /**
     * 获取设备列表
     * GET /api/equipment
     */
    @GetMapping
    public Result<List<Equipment>> getEquipmentList(
            @RequestParam(required = false) String status) {
        List<Equipment> equipment = equipmentService.getEquipmentList(status);
        return Result.success(equipment);
    }
}
