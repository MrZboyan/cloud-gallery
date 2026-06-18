package com.common.core.model.team.vo;

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
public class TeamVO implements Serializable {

    /**
     * id
     */
    private Long teamId;

    /**
     * 团队名称
     */
    private String name;

    /**
     * 团队简介
     */
    private String introduction;

    /**
     * 团队创建者
     */
    private Long ownerId;

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