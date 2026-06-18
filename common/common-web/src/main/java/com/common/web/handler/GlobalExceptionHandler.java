package com.common.web.handler;

import cn.dev33.satoken.exception.SaTokenException;
import com.common.core.common.result.BaseResponse;
import com.common.core.common.result.ResultUtils;
import com.common.core.exception.BusinessException;
import com.common.core.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 统一处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    public BaseResponse<?> sqlExceptionHandler(SQLException e) {
        log.error("SQLException", e);
        return ResultUtils.error(ErrorCode.DATABASE_ERROR, e.getMessage());
    }

    @ExceptionHandler(SaTokenException.class)
    public BaseResponse<?> saTokenExceptionHandler(SaTokenException e) {
        log.error("SaTokenException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * Dubbo RPC 异常统一处理
     */
    @ExceptionHandler(RpcException.class)
    public BaseResponse<?> rpcExceptionHandler(RpcException e) {
        log.error("RpcException: code={} message={}", e.getCode(), e.getMessage(), e);
        // 处理 Dubbo 基础设施异常
        return this.handleInfrastructureException(e);
    }

    /**
     * 统一处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.info("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }

    /**
     * 处理 Dubbo 基础设施异常（超时、网络、服务不可用等）
     */
    private BaseResponse<?> handleInfrastructureException(RpcException e) {
        // 超时异常
        if (e.isTimeout()) {
            log.warn("RPC 超时异常: {}", e.getMessage());
            return ResultUtils.error(ErrorCode.RPC_TIMEOUT_ERROR);
        }
        // 网络异常
        if (e.isNetwork()) {
            log.warn("RPC 网络异常: {}", e.getMessage());
            return ResultUtils.error(ErrorCode.RPC_NETWORK_ERROR);
        }
        // 服务不可用（No provider available）
        if (e.getMessage() != null && e.getMessage().contains("No provider available")) {
            log.warn("RPC 服务不可用: {}", e.getMessage());
            return ResultUtils.error(ErrorCode.RPC_SERVICE_UNAVAILABLE);
        }
        // 序列化异常
        if (e.isSerialization()) {
            log.error("RPC 序列化异常: {}", e.getMessage());
            return ResultUtils.error(ErrorCode.RPC_LIMIT_EXCEED_ERROR);
        }
        // 限流/熔断异常
        if (e.isLimitExceed()) {
            log.warn("RPC 限流/熔断: {}", e.getMessage());
            return ResultUtils.error(ErrorCode.RPC_LIMIT_EXCEED_ERROR);
        }
        // 其他 RPC 异常兜底
        log.error("未知 RPC 异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResultUtils.error(ErrorCode.RPC_UNKNOWN_ERROR);
    }
}