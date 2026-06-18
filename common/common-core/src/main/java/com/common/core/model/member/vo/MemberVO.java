package com.common.core.model.member.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MemberVO implements Serializable {

    /**
     * memberId
     */
    private Long memberId;

    /**
     * 团队 id
     */
    private Long teamId;

    /**
     * 成员 id
     */
    private Long userId;

    /**
     * 角色：member/editor/admin
     */
    private String role;

    @Serial
    private static final long serialVersionUID = 1L;

}
