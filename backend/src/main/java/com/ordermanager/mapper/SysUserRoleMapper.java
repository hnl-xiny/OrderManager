package com.ordermanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordermanager.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.UUID;

/**
 * 系统用户角色Mapper
 *
 * 继承 MyBatis-Plus BaseMapper，额外提供按用户名查询的方法。
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    /**
     * 根据用户名查询用户角色信息
     */
    SysUserRole selectByUsername(String username);
}
