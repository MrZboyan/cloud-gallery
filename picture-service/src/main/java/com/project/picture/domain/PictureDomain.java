package com.project.picture.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.core.model.picture.vo.PictureVO;
import com.project.picture.model.dto.*;
import com.project.picture.model.vo.StsTokenVO;

public interface PictureDomain {

    /**
     * 获取访问 url
     */
    String getPresignedGetUrl(Long pictureId);

    /**
     * 获取上传 url
     */
    String getPresignedPutUrl(Long galleryId, String fileName);

    /**
     * 获取 STS 临时凭证（前端直传用）
     */
    StsTokenVO getStsToken();

    /**
     * 上传图片（直传）
     */
    Long uploadPicture(PictureUploadDTO upload);

    /**
     * 删除图片
     */
    boolean deletePicture(PictureDeleteDTO delete);

    /**
     * 更新图片
     */
    boolean updatePicture(PictureUpdateDTO update);

    /**
     * 根据图片 id 获取图片信息 （用户）
     */
    PictureVO getPictureVOById(PictureQueryByIdDTO  queryById);

    /**
     * 分页获取图片列表（用户）
     */
    Page<PictureVO> listPictureVOByPage(PictureQueryDTO query);

}
