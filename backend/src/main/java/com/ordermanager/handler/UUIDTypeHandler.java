package com.ordermanager.handler;

import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.UUID;

/**
 * MyBatis TypeHandler：Java UUID ↔ PostgreSQL UUID 双向类型转换
 *
 * PostgreSQL 原生支持 UUID 类型，此 Handler 确保 MyBatis 在读写时正确映射。
 * 在实体类字段上通过 @TableField(typeHandler = UUIDTypeHandler.class) 引用。
 */
@MappedTypes(UUID.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class UUIDTypeHandler implements TypeHandler<UUID> {

    /** 将Java UUID写入PreparedStatement（转为PG的UUID对象） */
    @Override
    public void setParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
        if (parameter != null) {
            ps.setObject(i, parameter);
        } else {
            ps.setNull(i, Types.OTHER);
        }
    }

    /** 从ResultSet按列名读取 */
    @Override
    public UUID getResult(ResultSet rs, String columnName) throws SQLException {
        UUID uuid = rs.getObject(columnName, UUID.class);
        return uuid != null ? uuid : null;
    }

    /** 从ResultSet按列索引读取 */
    @Override
    public UUID getResult(ResultSet rs, int columnIndex) throws SQLException {
        UUID uuid = rs.getObject(columnIndex, UUID.class);
        return uuid != null ? uuid : null;
    }

    /** 从CallableStatement按列索引读取（存储过程/函数场景） */
    @Override
    public UUID getResult(CallableStatement cs, int columnIndex) throws SQLException {
        UUID uuid = cs.getObject(columnIndex, UUID.class);
        return uuid != null ? uuid : null;
    }
}
