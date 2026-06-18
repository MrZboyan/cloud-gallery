package com.user.api.service;

import com.common.core.model.user.entity.User;
import com.user.api.dto.UserLoginDTO;

import java.util.Collection;
import java.util.Set;

/**
 * 用户服务RPC接口
 * 注意：接口定义在api模块，实现在user-service模块
 */
public interface UserRpcService {

    /**
     * 验证用户登录
     *
     * @param request 登录请求
     * @return 用户信息
     */
    User login(UserLoginDTO request);

    /**
     * 获取登录用户信息
     */
    User getLoginUserInfo();

    /**
     * 根据 id 查询用户信息
     */
    User getUserById(Long userId);

    /**
     * 用户 id 集合
     */
    Collection<User> listByIds(Set<Long> userIdSet);
}