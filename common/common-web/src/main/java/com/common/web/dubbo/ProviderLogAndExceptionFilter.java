package com.common.web.dubbo;

import cn.dev33.satoken.same.SaSameUtil;
import cn.hutool.core.lang.UUID;
import com.common.core.common.constant.CommonConstant;
import com.common.core.exception.BusinessException;
import com.common.core.exception.ErrorCode;
import com.common.core.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.service.GenericService;
import org.apache.dubbo.rpc.support.RpcUtils;
import org.springframework.util.StopWatch;

import java.util.Map;

import static com.common.core.common.constant.CommonConstant.REQUEST_ID;

/**
 * 自定义 Dubbo 异常过滤器
 * 处理业务异常，避免将完整堆栈信息传递给消费端
 */
@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class ProviderLogAndExceptionFilter implements Filter, BaseFilter.Listener {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 从RPC上下文中获取Same-Token
        String token = RpcContext.getServerAttachment().getAttachment(SaSameUtil.SAME_TOKEN);
        boolean valid = SaSameUtil.isValid(token);
        ThrowUtils.throwIf(!valid, ErrorCode.FORBIDDEN_ERROR, "Same-Token 无效");
        // 生成请求 ID 或从 Consumer 传递过来
        String requestId = RpcContext.getServiceContext().getAttachment(REQUEST_ID);
        if (StringUtils.isBlank(requestId)) {
            log.info("RPC请求开始-- 获取请求ID失败，生成随机ID: {}", requestId);
            requestId = UUID.randomUUID().toString();
        }
        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 获取调用信息
        String serviceName = invoker.getInterface().getName();
        String methodName = invocation.getMethodName();
        Object[] args = invocation.getArguments();
        String remoteHost = RpcContext.getServiceContext().getRemoteHost();
        // 输出请求日志
        log.info("RPC请求开始-- 请求ID: {}, 方法: {}, 来源: {}, 参数: {}, 服务: {}",
                requestId, methodName, remoteHost, StringUtils.join(args, ", "), serviceName);
        Result result;
        try {
            // 执行实际调用
            result = invoker.invoke(invocation);
            // 检查是否有异常
            if (result.hasException()) {
                Throwable exception = result.getException();
                log.error("RPC请求异常-- ID: {}, 方法: {}, 异常: {}, 服务: {}",
                        requestId, methodName, exception.getMessage(), serviceName);
            }
            return result;
        } finally {
            stopWatch.stop();
            long totalTimeMillis = stopWatch.getTotalTimeMillis();
            log.info("RPC请求结束-- ID: {}, 耗时: {}ms 服务: {}, 方法: {}",
                    requestId, totalTimeMillis, serviceName, methodName);
        }
    }

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        // 如果没有异常或者是泛化调用，直接返回
        if (!appResponse.hasException() || GenericService.class == invoker.getInterface()) {
            return;
        }
        try {
            Throwable exception = appResponse.getException();
            // 优先处理 BusinessException
            if (exception instanceof BusinessException businessException) {
                Map<String, String> attachments = appResponse.getAttachments();
                attachments.put(CommonConstant.DUBBO_EXCEPTION_CODE, String.valueOf(businessException.getCode()));
                attachments.put(CommonConstant.DUBBO_EXCEPTION_MSG, businessException.getMessage());
                return;
            }
            // 如果不是 BusinessException，直接返回
            if (exception instanceof RpcException) {
                return;
            }
            // 记录未声明的异常
            log.error("Response Unchecked and undeclared anomalies: {}. service: {}, method: {}, exception: {}: {}",
                    RpcContext.getServiceContext().getRemoteHost(), invoker.getInterface().getName(),
                    RpcUtils.getMethodName(invocation), exception.getClass().getName(), exception.getMessage()
            );
        } catch (Throwable e) {
            log.warn("Failed to handle exception: {}. service: {}, method: {}, exception: {}: {}",
                    RpcContext.getServiceContext().getRemoteHost(), invoker.getInterface().getName(),
                    RpcUtils.getMethodName(invocation), e.getClass().getName(), e.getMessage(), e
            );
        }
    }

    @Override
    public void onError(Throwable throwable, Invoker<?> invoker, Invocation invocation) {
        log.error("PROVIDER Error: Unchecked and undeclared anomalies: {}. service: {}, method: {}, exception: {}: {}",
                RpcContext.getServiceContext().getRemoteHost(), invoker.getInterface().getName(),
                RpcUtils.getMethodName(invocation), throwable.getClass().getName(), throwable.getMessage());
    }
}