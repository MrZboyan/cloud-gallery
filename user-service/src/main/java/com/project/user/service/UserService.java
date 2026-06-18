package com.project.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.core.common.request.DeleteRequest;
import com.common.core.model.user.entity.User;
import com.common.core.model.user.vo.UserVO;
import com.project.user.model.dto.*;
import com.user.api.dto.UserLoginDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    long userRegister(UserRegisterDTO userRegisterRequest);

    /**
     * 用户登录
     */
    User userLogin(UserLoginDTO userLoginRequest, HttpServletRequest request);

    /**
     * 用户注销
     */
    boolean userLogout();

    /**
     * 获取当前登录用户
     */
    User getLoginUser();

    /**
     * 创建用户 仅管理员可用
     */
    User addUser(UserAddDTO userAddRequest);

    /**
     * 删除用户 仅管理员可用
     */
    boolean deleteUser(DeleteRequest deleteRequest);

    /**
     * 更新用户 仅管理员可用
     */
    boolean updateUser(UserUpdateDTO userUpdateRequest);

    /**
     * 根据 id 获取用户信息
     */
    User getUserById(Long userId);

    /**
     * 分页获取用户列表 仅管理员可用
     */
    Page<User> getUserByPage(UserQueryDTO userQueryRequest);

    /**
     * 获取脱敏的用户信息
     */
    Page<UserVO> getUserVOByPage(UserQueryDTO userQueryRequest);

    /**
     * 获取查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryRequest);

    /**
     * 修改密码
     */
    boolean updatePassword(UserUpdatePasswordDTO userUpdatePasswordRequest);

    /**
     * 修改用户信息
     */
    boolean updateMyUser(UserUpdateMyDTO userUpdateMyRequest);

    /**
     * 用户头像上传
     */
    String uploadAvatar(MultipartFile file);
}
