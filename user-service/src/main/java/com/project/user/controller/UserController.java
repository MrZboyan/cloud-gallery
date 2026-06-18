package com.project.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.core.common.annotation.AuthCheck;
import com.common.core.common.constant.UserConstant;
import com.common.core.common.converter.UserConverter;
import com.common.core.common.request.DeleteRequest;
import com.common.core.common.result.BaseResponse;
import com.common.core.common.result.ResultUtils;
import com.common.core.model.user.entity.User;
import com.common.core.model.user.vo.UserVO;
import com.project.user.model.dto.*;
import com.project.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户接口
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterDTO registerDTO) {
        long result = userService.userRegister(registerDTO);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/userinfo")
    public BaseResponse<UserVO> getLoginUser() {
        User user = userService.getLoginUser();
        return ResultUtils.success(UserConverter.INSTANCE.toVO(user));
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout() {
        return ResultUtils.success(userService.userLogout());
    }

    /**
     * 创建用户 仅管理员可用
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> addUser(@RequestBody UserAddDTO addDTO) {
        return ResultUtils.success(userService.addUser(addDTO));
    }

    /**
     * 删除用户 仅管理员可用
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        return ResultUtils.success(userService.deleteUser(deleteRequest));
    }

    /**
     * 更新用户 仅管理员可用
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateDTO updateDTO) {
        return ResultUtils.success(userService.updateUser(updateDTO));
    }

    /**
     * 根据 id 获取 用户仅管理员可用
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(@RequestParam long id) {
        return ResultUtils.success(userService.getUserById(id));
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(@RequestParam long id) {
        User user = userService.getUserById(id);
        return ResultUtils.success(UserConverter.INSTANCE.toVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryDTO queryDTO) {
        return ResultUtils.success(userService.getUserByPage(queryDTO));
    }

    /**
     * 分页获取用户封装列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryDTO queryDTO) {
        return ResultUtils.success(userService.getUserVOByPage(queryDTO));
    }

    /**
     * 更新个人信息
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyDTO updateMyDTO) {
        return ResultUtils.success(userService.updateMyUser(updateMyDTO));
    }

    /**
     * 更新当前登录用户密码
     */
    @PostMapping("/update/password")
    public BaseResponse<Boolean> updatePassword(@RequestBody UserUpdatePasswordDTO userUpdatePasswordDTO) {
        return ResultUtils.success(userService.updatePassword(userUpdatePasswordDTO));
    }

    /**
     * 上传头像
     */
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ResultUtils.success(userService.uploadAvatar(file));
    }

}
