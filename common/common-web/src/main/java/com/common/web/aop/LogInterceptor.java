package com.common.web.aop;

import cn.hutool.core.lang.UUID;
import com.common.web.utils.RequestIdContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class LogInterceptor {

    /**
     * 执行拦截
     */
    @Around("execution(* com.*..controller.*.*(..))")
    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 生成请求唯一 id
        String requestId = UUID.randomUUID().toString();
        // 存入 ThreadLocal 供 RPC Filter 使用
        RequestIdContext.set(requestId);
        String url = httpServletRequest.getRequestURI();
        // 获取请求参数
        Object[] args = point.getArgs();
        String reqParam = "[" + StringUtils.join(args, ", ") + "]";
        // 输出请求日志
        log.info("Start-请求ID: {}, 请求路径: {}, 请求IP: {}, 请求参数: {}",
                requestId, url, httpServletRequest.getRemoteHost(), reqParam);
        try {
            // 执行原方法
            return point.proceed();
        } finally {
            // 输出响应日志（无论是否发生异常都会执行）
            stopWatch.stop();
            long totalTimeMillis = stopWatch.getTotalTimeMillis();
            log.info("End-请求ID: {}, 耗时: {}ms", requestId, totalTimeMillis);
            RequestIdContext.remove();
        }
    }
}