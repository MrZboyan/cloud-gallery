package com.common.core.model.gallery.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图库
 */
@Data
@TableName(value = "gallery")
public class Gallery implements Serializable {
    /**
     * galleryId
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long galleryId;

    /**
     * 图库名称
     */
    private String name;

    /**
     * 图库简介
     */
    private String introduction;

    /**
     * 图库类型 COMMON-公共 SHARE-共享 TEAM-团队 PRIVATE-私人
     */
    private String type;

    /**
     * 等级 default-默认 mid-中级 high-高级 super-至尊（仅为公共图库享有）
     */
    private String level;

    /**
     * 已使用容量
     */
    private Long usedCapacity;

    /**
     * 最大容量（字节）默认 5 GB
     */
    private Long capacity;

    /**
     * 图库归属者
     */
    private Long ownerId;

    /**
     * 图库归属团队（仅当图库类型为团队时不为空）
     */
    private Long teamId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}