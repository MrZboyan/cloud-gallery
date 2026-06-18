package com.project.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.common.core.common.converter.UserConverter;
import com.common.core.model.user.entity.User;
import com.common.core.model.user.vo.UserVO;
import com.project.auth.service.AuthService;
import com.user.api.dto.UserLoginDTO;
import com.user.api.service.UserRpcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import static com.common.core.common.constant.UserConstant.USER_LOGIN_STATE;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /**
     * 引用远程 Dubbo 服务
     * DubboReference 注解说明：
     * - version: 服务版本号（必须与提供者一致）
     * - timeout: 调用超时时间
     * - check: 启动时是否检查服务可用
     * - retries: 重试次数
     * - loadbalance: 负载均衡策略
     * - mock: 服务降级（可选）
     */
    @DubboReference(version = "1.0.0")
    private UserRpcService userRpcService;

    /**
     * 用户登录（密码）
     */
    @Override
    public UserVO login(UserLoginDTO request) {
        // 检查当前会话是否已登录
        if (StpUtil.isLogin()) {
            // 如果已登录，先登出旧会话
            StpUtil.logout();
        }
        User login = userRpcService.login(request);
        // 记录登录态 生成Token
        StpUtil.login(login.getUserId());
        StpUtil.getSession().set(USER_LOGIN_STATE, login);
        return UserConverter.INSTANCE.toVO(login);
    }

    /**
     * 用户登出
     */
    @Override
    public boolean logout() {
        StpUtil.getSession().logout();
        StpUtil.logout();
        return true;
    }

    /**
     * 获取当前登录用户信息
     */
    @Override
    public UserVO getLoginUser() {
        User login = (User) StpUtil.getSession().get(USER_LOGIN_STATE);
        return UserConverter.INSTANCE.toVO(login);
    }


}
