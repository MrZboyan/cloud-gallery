package com.project.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.core.model.user.entity.User;
import com.project.user.service.UserService;
import com.user.api.dto.UserLoginDTO;
import com.user.api.service.UserRpcService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;
import java.util.Set;

/**
 * 用户服务Dubbo实现
 * DubboService 注解说明：
 * - version: 服务版本号（建议使用）
 * - timeout: 超时时间（毫秒）
 * - retries: 重试次数（0表示不重试）
 * - loadbalance: 负载均衡策略
 * - interfaceClass: 接口类（可选，一般可以自动推断）
 */
@Slf4j
@DubboService(version = "1.0.0", timeout = 3000, retries = 0)
public class UserRpcServiceImpl implements UserRpcService {

    @Resource
    private UserService userService;

    /**
     * 用户登录
     */
    @Override
    public User login(UserLoginDTO login) {
        return userService.userLogin(login, null);
    }

    /**
     * 获取登录用户信息
     */
    @Override
    public User getLoginUserInfo() {
        return userService.getLoginUser();
    }

    @Override
    public User getUserById(Long userId) {
        return userService.getOne(new QueryWrapper<User>().eq("userId", userId));
    }

    @Override
    public Collection<User> listByIds(Set<Long> userIdSet) {
        if (userIdSet != null && !userIdSet.isEmpty()) {
            return userService.listByIds(userIdSet);
        }
        return null;
    }

}