package com.project.picture.service.impl;

import com.project.picture.upload.OssManage;
import com.upload.api.UploadRpcService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 上传服务 Dubbo 实现
 * DubboService 注解说明：
 * - version: 服务版本号（建议使用）
 * - timeout: 超时时间（毫秒）
 * - retries: 重试次数（0表示不重试）
 * - loadbalance: 负载均衡策略
 * - interfaceClass: 接口类（可选，一般可以自动推断）
 */
@Slf4j
@DubboService(version = "1.0.0", timeout = 5000, retries = 0)
public class UploadRpcServiceImpl implements UploadRpcService {

    @Resource
    private OssManage ossManage;

    /**
     * 上传文件 （用户头像）
     */
    @Override
    public String upload(byte[] bytes, String filePathName) {
        return ossManage.upload(bytes, bytes.length, filePathName);
    }
}
