package com.common.core.model.gallery.vo;

import com.common.core.model.user.vo.UserVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户视图（脱敏）
 */
@Data
public class GalleryVO implements Serializable {

    /**
     * 图库id
     */
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
     * 用户 VO
     */
    private UserVO userVO;

    @Serial
    private static final long serialVersionUID = 1L;
}