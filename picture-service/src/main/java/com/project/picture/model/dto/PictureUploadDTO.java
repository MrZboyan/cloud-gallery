package com.project.picture.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PictureUploadDTO implements Serializable {

    /**
     * 图库 id
     */
    private Long galleryId;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private List<String> category;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;

    /**
     * 对象存储中的路径
     * 例如 gallery/12/2025-01-01-uuid.jpg
     */
    private String objectPath;

    @Serial
    private static final long serialVersionUID = 1L;
}
