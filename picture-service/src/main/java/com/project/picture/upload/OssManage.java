package com.project.picture.upload;

import cn.hutool.core.io.FileUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.common.core.exception.BusinessException;
import com.common.core.exception.ErrorCode;
import com.project.picture.config.OssConfig;
import com.project.picture.utils.ImageUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Component
public class OssManage {

    @Resource
    private OssConfig ossConfig;

    @Resource
    private OSS ossClient;

    /**
     * 获取对象元信息
     */
    public ObjectMetadata getObjectMeta(String objectPath, boolean isPublic) {
        try {
            String bucket = isPublic ? ossConfig.getBucketPublicName() : ossConfig.getBucketPrivateName();
            return ossClient.getObjectMetadata(bucket, objectPath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OSS_GET_INFO_ERROR, "获取对象信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取对象流并解析图片信息
     */
    public ImageUtils.ImageMeta getObjectMetaAndParseImage(String objectPath, boolean isPublic) {
        String bucket = isPublic ? ossConfig.getBucketPublicName() : ossConfig.getBucketPrivateName();
        try (InputStream inputStream = ossClient.getObject(bucket, objectPath).getObjectContent()) {
            return ImageUtils.getImageMeta(inputStream, objectPath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OSS_GET_OBJ_ERROR, "获取对象失败：" + e.getMessage());
        }
    }

    /**
     * 获取下载签名 URL
     */
    public String getPresignedUrlForGet(String objectPath, boolean isPublic) {
        try {
            String bucket = isPublic ? ossConfig.getBucketPublicName() : ossConfig.getBucketPrivateName();
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, objectPath);
            request.setExpiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000L));
            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OSS_GET_URL_ERROR, e.getMessage());
        }
    }

    /**
     * 获取上传签名 URL
     */
    public String getPresignedUrlForPut(String objectPath, boolean isPublic) {
        try {
            String bucket = isPublic ? ossConfig.getBucketPublicName() : ossConfig.getBucketPrivateName();
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, objectPath);
            request.setMethod(com.aliyun.oss.HttpMethod.PUT);
            request.setExpiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000L));
            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OSS_PUT_URL_ERROR, e.getMessage());
        }
    }

    /**
     * 上传文件（用户头像）
     */
    public String upload(byte[] bytes, long size, String filePathName) {
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(size);
            metadata.setContentType(FileUtil.getMimeType(filePathName));
            ossClient.putObject(ossConfig.getBucketPublicName(), filePathName, inputStream, metadata);
            return getFileUrl(filePathName);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OSS_UPLOAD_ERROR, "上传失败：" + e.getMessage());
        }
    }

    /**
     * 返回上传后的文件 URL
     */
    private String getFileUrl(String fileName) {
        return "https://" + ossConfig.getBucketPublicName() + "." + ossConfig.getEndpoint() + "/" + fileName;
    }
}
