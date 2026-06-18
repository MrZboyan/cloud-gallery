package com.project.picture.upload;

import com.aliyun.sts20150401.Client;
import com.aliyun.sts20150401.models.AssumeRoleRequest;
import com.aliyun.sts20150401.models.AssumeRoleResponse;
import com.aliyun.teaopenapi.models.Config;
import com.common.core.exception.BusinessException;
import com.common.core.exception.ErrorCode;
import com.project.picture.config.OssConfig;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class StsManage {

    @Resource
    private OssConfig ossConfig;

    @Data
    public static class StsCredentials {
        private String accessKeyId;
        private String accessKeySecret;
        private String securityToken;
        private String expiration;
    }

    /**
     * 获取 STS 临时凭证
     */
    public StsCredentials getStsToken() {
        try {
            Config config = new Config()
                    .setAccessKeyId(ossConfig.getAccessKeyId())
                    .setAccessKeySecret(ossConfig.getAccessKeySecret())
                    .setEndpoint("sts.cn-hangzhou.aliyuncs.com");
            Client stsClient = new Client(config);

            AssumeRoleRequest request = new AssumeRoleRequest()
                    .setRoleArn(ossConfig.getRoleArn())
                    .setRoleSessionName(ossConfig.getRoleSessionName())
                    .setDurationSeconds(ossConfig.getStsDurationSeconds());

            AssumeRoleResponse response = stsClient.assumeRole(request);
            var body = response.getBody();

            StsCredentials credentials = new StsCredentials();
            credentials.setAccessKeyId(body.getCredentials().getAccessKeyId());
            credentials.setAccessKeySecret(body.getCredentials().getAccessKeySecret());
            credentials.setSecurityToken(body.getCredentials().getSecurityToken());
            credentials.setExpiration(body.getCredentials().getExpiration());
            return credentials;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OSS_STS_ERROR, "获取STS临时凭证失败：" + e.getMessage());
        }
    }
}
