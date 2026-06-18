package com.common.core.exception;

import lombok.Getter;

/**
 * 自定义错误码
 */
@Getter
public enum ErrorCode {

    // 业务相关错误
    PARAMS_ERROR(410, "请求参数错误"),
    NOT_LOGIN_ERROR(411, "未登录"),
    NO_AUTH_ERROR(412, "无权限"),
    NOT_FOUND_ERROR(413, "请求数据不存在"),
    FORBIDDEN_ERROR(414, "禁止访问"),
    TOO_MANY_REQUEST(415, "请求过于频繁"),
    // Sa-Token 错误
    SA_TOKEN_ERROR(420, "Sa-Token 错误"),
    // OSS 相关
    OSS_UPLOAD_ERROR(430, "上传失败"),
    OSS_DOWNLOAD_ERROR(431, "下载失败"),
    OSS_DELETE_ERROR(432, "删除失败"),
    OSS_GET_URL_ERROR(433, "获取访问URL失败"),
    OSS_PUT_URL_ERROR(434, "获取上传URL失败"),
    OSS_GET_INFO_ERROR(435, "获取文件信息失败"),
    OSS_GET_OBJ_ERROR(436, "获取对象失败"),
    OSS_STS_ERROR(437, "获取STS临时凭证失败"),
    // 系统错误
    SYSTEM_ERROR(510, "系统内部异常"),
    OPERATION_ERROR(511, "操作失败"),
    DATABASE_ERROR(512, "数据库异常"),
    // RPC 错误
    RPC_TIMEOUT_ERROR(520, "服务调用超时"),
    RPC_SERVICE_UNAVAILABLE(521, "服务暂时不可用"),
    RPC_NETWORK_ERROR(522, "网络连接异常"),
    RPC_SERIALIZATION_ERROR(523, "序列化异常"),
    RPC_LIMIT_EXCEED_ERROR(524, "服务调用次数超出限制"),
    RPC_UNKNOWN_ERROR(525, "未知错误");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}