package com.project.picture.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PictureUpdateDTO implements Serializable {

    /**
     * 图库 id
     */
    private Long galleryId;

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类（JSON 数组）
     */
    private List<String> category;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;

    @Serial
    private static final long serialVersionUID = 1L;
}
