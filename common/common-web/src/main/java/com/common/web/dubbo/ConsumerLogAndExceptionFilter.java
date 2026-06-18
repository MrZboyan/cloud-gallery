package com.common.web.dubbo;

import cn.dev33.satoken.same.SaSameUtil;
import com.common.core.common.constant.CommonConstant;
import com.common.core.exception.BusinessException;
import com.common.web.utils.RequestIdContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.support.RpcUtils;
import org.springframework.util.StopWatch;

import java.util.Map;

import static com.common.core.common.constant.CommonConstant.REQUEST_ID;

@Slf4j
@Activate(group = {CommonConstants.CONSUMER})
public class ConsumerLogAndExceptionFilter implements Filter, BaseFilter.Listener {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 将Same-Token添加到RPC上下文中
        RpcContext.getClientAttachment().setAttachment(SaSameUtil.SAME_TOKEN, SaSameUtil.getToken());
        // 从当前线程上下文获取 requestId 由 Controller 的 AOP 设置
        String requestId = RpcContext.getClientAttachment().getAttachment(REQUEST_ID);
        // 如果没有 则使用当前线程的 requestId 通过 ThreadLocal 传递
        if (StringUtils.isBlank(requestId)) {
            requestId = RequestIdContext.get();
        }
        // 将 requestId 传递给 Provider
        if (StringUtils.isNotBlank(requestId)) {
            RpcContext.getClientAttachment().setAttachment(REQUEST_ID, requestId);
        }
        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 获取调用信息
        String serviceName = invoker.getInterface().getName();
        String methodName = invocation.getMethodName();
        Object[] args = invocation.getArguments();
        // 输出请求日志
        log.info("RPC请求开始-- 请求ID: {}, 服务: {}, 方法: {}, 参数: {}",
                requestId, serviceName, methodName, StringUtils.join(args, ", "));
        Result result;
        try {
            // 执行实际调用
            result = invoker.invoke(invocation);
            // 检查是否有异常
            if (result.hasException()) {
                Throwable exception = result.getException();
                log.error("调用RPC异常-- ID: {}, 方法: {}, 异常: {}, 服务: {}",
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
        Map<String, String> attachments = appResponse.getAttachments();
        String code = attachments.get(CommonConstant.DUBBO_EXCEPTION_CODE);
        String message = attachments.get(CommonConstant.DUBBO_EXCEPTION_MSG);
        if (code != null && message != null) {
            appResponse.setException(new BusinessException(Integer.parseInt(code), message));
        }
    }

    @Override
    public void onError(Throwable throwable, Invoker<?> invoker, Invocation invocation) {
        log.error("CONSUMER Error: Unchecked and undeclared anomalies: {}. service: {}, method: {}, exception: {}: {}",
                RpcContext.getServiceContext().getRemoteHost(), invoker.getInterface().getName(),
                RpcUtils.getMethodName(invocation), throwable.getClass().getName(), throwable.getMessage());

    }
}