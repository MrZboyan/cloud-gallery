package com.project.picture.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PictureQueryByIdDTO implements Serializable {

    /**
     * 图库 id
     */
    private Long galleryId;

    /**
     * 图片 id
     */
    private Long pictureId;

    @Serial
    private static final long serialVersionUID = 1L;
}
