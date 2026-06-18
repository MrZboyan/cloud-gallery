package com.project.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.core.model.picture.entity.Picture;
import com.common.core.model.picture.vo.PictureVO;
import com.common.core.model.user.entity.User;
import com.project.picture.model.dto.PictureQueryDTO;
import com.project.picture.model.dto.PictureUpdateDTO;
import com.project.picture.model.dto.PictureUploadDTO;

/**
 * @author Zangdibo
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片（直传）
     */
    Long uploadPicture(PictureUploadDTO upload, User loginUser, boolean isPublic);

    /**
     * 删除图片
     */
    boolean deletePicture(Long pictureId);

    /**
     * 更新图片
     */
    boolean updatePicture(PictureUpdateDTO update, User loginUser);

    /**
     * 根据图片 id 获取图片 （管理员）
     */
    Picture getPictureById(Long pictureId);

    /**
     * 根据图片 id 获取图片 （用户）
     */
    PictureVO getPictureVOById(Long pictureId, User loginUser);

    /**
     * 分页获取图片列表（管理员）
     */
    Page<Picture> listPictureByPage(PictureQueryDTO query);

    /**
     * 分页获取图片列表（用户）
     */
    Page<PictureVO> listPictureVOByPage(PictureQueryDTO query);

    /**
     * 获取查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryDTO pictureQueryRequest);

}
