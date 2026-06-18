package com.project.picture.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PictureDeleteDTO implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 图库 id
     */
    private Long galleryId;

    @Serial
    private static final long serialVersionUID = 1L;
}
