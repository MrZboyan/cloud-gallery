package com.common.web.aop;

import cn.dev33.satoken.stp.StpUtil;
import com.common.core.common.annotation.AuthCheck;
import com.common.core.common.enums.UserRoleEnum;
import com.common.core.exception.BusinessException;
import com.common.core.exception.ErrorCode;
import com.common.core.model.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import static com.common.core.common.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 权限校验 AOP
 * 切面类
 */
@Slf4j
@Aspect
@Component
public class AuthInterceptor {

    /**
     * 执行拦截
     *
     * @param joinPoint 连接点
     * @param authCheck 角色注解
     * @return 放行
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        User loginUser = null;
        String userRole = null;
        try {
            // 获取注解中的角色信息
            String mustRole = authCheck.mustRole();
            log.info("权限校验开始--需要权限: {}", mustRole);
            // 获取登录用户
            loginUser = (User) StpUtil.getSession().get(USER_LOGIN_STATE);
            userRole = loginUser.getUserRole();
            // 逻辑校验
            UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            // 到此处一定为登录用户 为空则放行
            if (mustRoleEnum == null) {
                return joinPoint.proceed();
            }
            UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(userRole);
            if (userRoleEnum == null || UserRoleEnum.BAN.equals(userRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            log.info("权限校验通过--用户ID: {}, 用户权限: {}", loginUser.getUserId(), userRole);
            // 通过校验 放行
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            // 在任何情况下都记录耗时
            if (loginUser != null && userRole != null) {
                log.info("权限校验结束--用户ID: {}, 用户权限: {}, 耗时: {}ms",
                        loginUser.getUserId(), userRole, stopWatch.getTotalTimeMillis());
            } else {
                log.info("权限校验结束--耗时: {}ms", stopWatch.getTotalTimeMillis());
            }
        }
    }
}
