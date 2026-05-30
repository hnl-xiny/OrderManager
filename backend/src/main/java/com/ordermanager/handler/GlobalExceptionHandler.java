package com.ordermanager.handler;

import com.ordermanager.dto.Result;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.badRequest(message);
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.badRequest(message);
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.badRequest(e.getMessage());
    }

    /**
     * 权限不足异常
     */
    @ExceptionHandler(PermissionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handlePermissionDeniedException(PermissionDeniedException e) {
        return Result.forbidden(e.getMessage());
    }

    /**
     * 重复订单异常（409 Conflict）
     */
    @ExceptionHandler(DuplicateOrderException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<?> handleDuplicateOrderException(DuplicateOrderException e) {
        return Result.conflict(e.getMessage());
    }

    /**
     * MyBatis 系统异常
     */
    @ExceptionHandler(org.mybatis.spring.MyBatisSystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleMyBatisSystemException(org.mybatis.spring.MyBatisSystemException e) {
        Throwable cause = e.getCause();
        String detail = cause != null ? cause.getClass().getSimpleName() + ": " + cause.getMessage() : e.getMessage();
        return Result.error("数据库操作失败: " + detail);
    }

    /**
     * 通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        String detail = e.getClass().getName() + ": " + (e.getMessage() == null ? "(无详细信息)" : e.getMessage());
        return Result.error("服务器内部错误: " + detail);
    }

    /**
     * 类型转换异常（通常由 Redis 反序列化或 MyBatis 映射错误引起）
     */
    @ExceptionHandler(ClassCastException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleClassCastException(ClassCastException e) {
        String message = e.getMessage();
        if (message != null && message.contains("LinkedHashMap") && message.contains("Order")) {
            return Result.badRequest("今日已存在相同客户和设备的订单");
        }
        return Result.badRequest("数据类型处理异常: " + message);
    }

    /**
     * 业务异常
     */
    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }

    /**
     * 权限不足异常
     */
    public static class PermissionDeniedException extends RuntimeException {
        public PermissionDeniedException(String message) {
            super(message);
        }
    }

    /**
     * 重复订单异常
     */
    public static class DuplicateOrderException extends RuntimeException {
        public DuplicateOrderException(String message) {
            super(message);
        }
    }
}
