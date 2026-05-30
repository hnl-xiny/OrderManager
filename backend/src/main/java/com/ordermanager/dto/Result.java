package com.ordermanager.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一响应结果封装
 *
 * 前后端约定的标准响应格式：{ code, message, data, timestamp }
 * code 200 表示成功，其余为业务错误码（400/401/403/409/500）
 */
@Data
public class Result<T> {
    /** 业务状态码：200成功，4xx客户端错误，5xx服务端错误 */
    private Integer code;
    /** 友好提示信息 */
    private String message;
    /** 响应数据体 */
    private T data;
    /** 响应时间戳（毫秒），用于排查链路延迟 */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> unauthorized(String message) {
        return error(401, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return error(403, message);
    }

    public static <T> Result<T> conflict(String message) {
        return error(409, message);
    }

    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }
}
