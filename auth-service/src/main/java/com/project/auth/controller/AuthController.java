package com.project.auth.controller;

import com.common.core.common.result.BaseResponse;
import com.common.core.common.result.ResultUtils;
import com.common.core.model.user.vo.UserVO;
import com.project.auth.service.AuthService;
import com.user.api.dto.UserLoginDTO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证登录
 */
@RestController
@RequestMapping("/oauth2")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login")
    public BaseResponse<UserVO> login(@RequestBody UserLoginDTO login) {
        return ResultUtils.success(authService.login(login));
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> logout() {
        boolean logout = authService.logout();
        return ResultUtils.success(logout);
    }

}
