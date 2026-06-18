package com.project.auth.service;

import com.common.core.model.user.vo.UserVO;
import com.user.api.dto.UserLoginDTO;

public interface AuthService {

    /**
     * 登录
     */
    UserVO login(UserLoginDTO login);

    /**
     * 登出
     */
    boolean logout();

    /**
     * 获取当前登录用户
     */
    UserVO getLoginUser();
}
