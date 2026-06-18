package com.project.user.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.common.constant.CommonConstant;
import com.common.core.common.converter.UserConverter;
import com.common.core.common.enums.UserRoleEnum;
import com.common.core.common.request.DeleteRequest;
import com.common.core.common.utils.IdUtils;
import com.common.core.common.utils.SqlUtils;
import com.common.core.exception.BusinessException;
import com.common.core.exception.ErrorCode;
import com.common.core.exception.ThrowUtils;
import com.common.core.model.user.entity.User;
import com.common.core.model.user.vo.UserVO;
import com.common.web.utils.DistributedLockUtil;
import com.project.user.model.dto.*;
import com.project.user.model.mapper.UserMapper;
import com.project.user.service.UserService;
import com.upload.api.UploadRpcService;
import com.user.api.dto.UserLoginDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.common.core.common.constant.UserConstant.*;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 混淆
     */
    public static final String SALT = "picture-cloud";

    @Resource
    private DistributedLockUtil distributedLockUtil;

    @DubboReference(version = "1.0.0")
    private UploadRpcService uploadRpcService;

    /**
     * 用户注册
     */
    @Override
    public long userRegister(UserRegisterDTO userRegisterRequest) {
        if (ObjectUtils.isEmpty(userRegisterRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 获取分布式锁
        User user = distributedLockUtil.execute("lock:user:register:" + userAccount, () -> {
            // 尝试创建用户
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 插入数据
            User tempUser = new User();
            tempUser.setUserAccount(userAccount);
            tempUser.setUserPassword(encryptPassword);
            tempUser.setUserName("用户" + RandomUtil.randomString(6));
            tempUser.setUserAvatar(USER_AVATAR_URL);
            tempUser.setUserProfile(USER_INTRO);
            tempUser.setUserRole(UserRoleEnum.USER.getValue());
            tempUser.setCreateTime(LocalDateTime.now());
            tempUser.setUpdateTime(LocalDateTime.now());
            try {
                this.save(tempUser);
            } catch (DuplicateKeyException e) {
                // DB 唯一索引兜底
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "创建用户失败,请重试。");
            }
            return tempUser;
        });
        return user.getUserId();
    }

    /**
     * 用户登录
     */
    @Override
    public User userLogin(UserLoginDTO login, HttpServletRequest request) {
        if (ObjectUtils.isEmpty(login)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = login.getUserAccount();
        String userPassword = login.getUserPassword();
        // 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        User user = this.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
        ThrowUtils.throwIf(ObjectUtils.isEmpty(user), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        boolean equals = user.getUserPassword().equals(encryptPassword);
        ThrowUtils.throwIf(!equals, ErrorCode.NOT_FOUND_ERROR, "密码错误");
        // 脱敏后返回用户数据
        return user;
    }

    /**
     * 用户注销
     */
    @Override
    public boolean userLogout() {
        StpUtil.getSession().logout();
        StpUtil.logout();
        return true;
    }

    /**
     * 获取当前登录用户
     *
     * @return 用户
     */
    @Override
    public User getLoginUser() {
        // 获取 Session
        SaSession session = StpUtil.getSession();
        ThrowUtils.throwIf(ObjectUtils.isEmpty(session), ErrorCode.NOT_LOGIN_ERROR);
        // 获取当前登录用户
        User user = (User) session.get(USER_LOGIN_STATE);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(user), ErrorCode.NOT_LOGIN_ERROR);
        // 转换为实体
        if (user.getUserRole().equals(UserRoleEnum.BAN.getValue())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "用户被封禁");
        }
        // 检查是否需要刷新数据
        Boolean needRefresh = (Boolean) session.get(NEED_REFRESH_USER);
        if (Boolean.TRUE.equals(needRefresh)) {
            // 从数据库查询最新数据
            user = this.getOne(new QueryWrapper<User>().eq("userId", user.getUserId()));
            ThrowUtils.throwIf(ObjectUtils.isEmpty(user), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            // 更新缓存 清除标志
            session.set(USER_LOGIN_STATE, user);
            session.delete(NEED_REFRESH_USER);
        }
        return user;
    }

    /**
     * 创建用户 仅管理员可用
     */
    @Override
    public User addUser(UserAddDTO userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((UserServiceImpl.SALT + defaultPassword).getBytes());
        // 随机生成用户名 用时间戳区分 以防重名
        String username = "用户" + System.currentTimeMillis() % 100000 + RandomUtil.randomString(5);
        user.setUserName(username);
        user.setUserPassword(encryptPassword);
        user.setUserAvatar(USER_AVATAR_URL);
        user.setUserProfile("这个人还没有用户简介~");
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.DATABASE_ERROR);
        return user;
    }

    /**
     * 删除用户 仅管理员可用
     */
    @Override
    public boolean deleteUser(DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = this.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.DATABASE_ERROR);
        return true;
    }

    /**
     * 更新用户 仅管理员可用
     */
    @Override
    public boolean updateUser(UserUpdateDTO userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.DATABASE_ERROR);
        return true;
    }

    /**
     * 根据 id 获取用户
     */
    @Override
    public User getUserById(Long userId) {
        User user = this.getOne(new QueryWrapper<User>().eq("userId", userId)
                .select("userId", "userAccount", "userName", "userAvatar",
                        "userProfile", "userRole", "createTime", "updateTime", "isDelete"));
        ThrowUtils.throwIf(ObjectUtils.isEmpty(user), ErrorCode.NOT_FOUND_ERROR);
        return user;
    }

    /**
     * 获取用户列表 （管理员）
     */
    @Override
    public Page<User> getUserByPage(UserQueryDTO userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        return this.page(new Page<>(current, size), this.getQueryWrapper(userQueryRequest));
    }

    /**
     * 获取用户列表 VO
     */
    @Override
    public Page<UserVO> getUserVOByPage(UserQueryDTO userQueryRequest) {
        ThrowUtils.throwIf(ObjectUtils.isEmpty(userQueryRequest), ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = this.page(new Page<>(current, size),
                this.getQueryWrapper(userQueryRequest));
        List<User> userPageRecords = userPage.getRecords();
        List<UserVO> userVOList = userPageRecords.stream().map(UserConverter.INSTANCE::toVO).toList();
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    /**
     * 获取查询条件
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        // 填充默认值
        Long userId = userQueryRequest.getUserId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        // 构造查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(IdUtils.checkId(userId), "userId", userId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_DESC),
                sortField);
        return queryWrapper;
    }

    /**
     * 修改密码
     */
    @Override
    public boolean updatePassword(UserUpdatePasswordDTO userUpdatePasswordRequest) {
        // 校验
        ThrowUtils.throwIf(ObjectUtils.isEmpty(userUpdatePasswordRequest), ErrorCode.PARAMS_ERROR, "参数为空");
        // 获取登录用户
        User loginUser = this.getLoginUser();
        String newPassword = userUpdatePasswordRequest.getNewPassword();
        String checkPassword = userUpdatePasswordRequest.getCheckPassword();
        // 两次密码
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        String oldPassword = userUpdatePasswordRequest.getOldPassword();
        // 验证旧密码
        String asHexOldPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        if (!loginUser.getUserPassword().equals(asHexOldPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码错误");
        }
        // 验证密码规范
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (oldPassword.equals(newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码和旧密码不能相同");
        }
        // 加密新密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        // 插入数据
        User user = new User();
        user.setUserId(loginUser.getUserId());
        user.setUserPassword(encryptPassword);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新失败，数据库错误");
        // 修改成功移除登录态
        this.userLogout();
        return true;
    }

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest 修改信息
     */
    @Override
    public boolean updateMyUser(UserUpdateMyDTO userUpdateMyRequest) {
        User loginUser = this.getLoginUser();
        // 创建 user 对象 并将当前用户更新的信息拷贝到 user 对象中
        User newUser = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, newUser);
        // 设置user对象的id为当前登录用户的id 并设置user对象的userRole为当前登录用户的userRole
        newUser.setUserId(loginUser.getUserId());
        // 判断是否有空字段，如果为空则填充默认值
        if (newUser.getUserAvatar() == null) {
            newUser.setUserAvatar(USER_AVATAR_URL);
        }
        // 执行更新用户数据
        boolean result = this.updateById(newUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 为 session 设置标志指示需要重新加载用户数据
        StpUtil.getSession().set(NEED_REFRESH_USER, true);
        return true;
    }

    /**
     * 用户头像上传
     */
    @Override
    public String uploadAvatar(MultipartFile file) {
        String uuid = RandomUtil.randomString(16);
        String originFilename = file.getOriginalFilename();
        String uploadPath = String.format("%s-%s-avatar.%s", LocalDate.now(), uuid, FileUtil.getSuffix(originFilename));
        try {
            return uploadRpcService.upload(file.getBytes(), uploadPath);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }

}
