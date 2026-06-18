package com.common.core.common.constant;

/**
 * 用户常量
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    /**
     * 需要刷新用户信息
     */
    String NEED_REFRESH_USER = "need_refresh_user";

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    /**
     * 默认头像
     */
    String USER_AVATAR_URL = "http://www.ztmiaowu.online:9000/cloud-gallery/userAvatar.png";

    /**
     * 默认简介
     */
    String USER_INTRO = "这个人很懒，什么都没写";
}