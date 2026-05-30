package com.ordermanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ordermanager.handler.UUIDTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 系统用户角色实体类
 * 对应表: sys_user_role
 *
 * 注意：此处采用用户即角色的简化设计，username 字段存储系统登录账号
 */
@Data
@TableName(value = "sys_user_role", autoResultMap = true)
public class SysUserRole {
    /** 用户唯一标识（UUID），插入时自动分配 */
    @TableId(value = "user_id", type = IdType.ASSIGN_UUID)
    @TableField(value = "user_id", typeHandler = UUIDTypeHandler.class)
    private UUID userId;

    /** 角色名称：admin-管理员 / operator-操作员 */
    @TableField("role_name")
    private String roleName;

    /** 角色描述 */
    @TableField("role_desc")
    private String roleDesc;

    /** 用户登录账号 */
    @TableField("username")
    private String username;

    /** 账号状态：normal-正常 / disabled-已禁用 */
    @TableField("status")
    private String status;

    /** 记录创建时间，插入时自动填充 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 最近一次更新时间，更新时自动填充 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
