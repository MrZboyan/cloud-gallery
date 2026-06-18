package com.upload.api;

public interface UploadRpcService {

    /**
     * 上传文件 UserAvatar
     */
    String upload(byte[] bytes, String filePathName);

}
