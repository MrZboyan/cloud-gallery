package com.common.core.model.picture.vo;

import com.common.core.model.user.vo.UserVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PictureVO implements Serializable {

    /**
     * id
     */
    private Long pictureId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 图库 id
     */
    private Long galleryId;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 图片地址 缩略图
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 分类
     */
    private List<String> category;

    /**
     * 文件大小
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 编辑时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime editTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建用户信息
     */
    private UserVO userVO;

    @Serial
    private static final long serialVersionUID = 1L;
}