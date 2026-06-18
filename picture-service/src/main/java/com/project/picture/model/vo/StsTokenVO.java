package com.project.picture.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class StsTokenVO implements Serializable {

    private String accessKeyId;

    private String accessKeySecret;

    private String securityToken;

    private String expiration;

    @Serial
    private static final long serialVersionUID = 1L;
}
