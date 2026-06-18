package com.project.picture.domain.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.core.common.constant.CommonConstant;
import com.common.core.common.utils.PicFormatUtils;
import com.common.core.exception.ErrorCode;
import com.common.core.exception.ThrowUtils;
import com.common.core.model.picture.entity.Picture;
import com.common.core.model.picture.vo.PictureVO;
import com.common.core.model.user.entity.User;
import com.project.picture.domain.PictureDomain;
import com.project.picture.model.dto.*;
import com.project.picture.model.vo.StsTokenVO;
import com.project.picture.service.PictureService;
import com.project.picture.upload.OssManage;
import com.project.picture.upload.StsManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.common.core.common.constant.UserConstant.USER_LOGIN_STATE;

@Service
public class PictureDomainImpl implements PictureDomain {

    @Resource
    private PictureService pictureService;

    @Resource
    private OssManage ossManage;

    @Resource
    private StsManage stsManage;

    /**
     * 获取图片访问链接
     */
    @Override
    public String getPresignedGetUrl(Long pictureId) {
        // 获取登陆用户信息
        User loginUser = (User) StpUtil.getSession().get(USER_LOGIN_STATE);
        // 校验用户是否有权限访问该图片 todo 需要先完善用户权限快照
        // 校验图片是否存在
        Picture picture = pictureService.getOne(new QueryWrapper<Picture>().eq("pictureId", pictureId));
        ThrowUtils.throwIf(ObjectUtils.isEmpty(picture), ErrorCode.NOT_FOUND_ERROR);
        // 校验完毕拿到图片信息
        String objectPath = picture.getUrl();
        // 图片所属图库
        Long galleryId = picture.getGalleryId();
        boolean isPublic = CommonConstant.COMMON_GALLERY_ID.equals(galleryId);
        return ossManage.getPresignedUrlForGet(objectPath, isPublic);
    }

    /**
     * 获取图片上传链接
     */
    @Override
    public String getPresignedPutUrl(Long galleryId, String fileName) {
        // 判断 fileName 是否合法
        String format = PicFormatUtils.detectFormat(fileName);
        boolean result = PicFormatUtils.verifyFormat(format);
        ThrowUtils.throwIf(!result, ErrorCode.PARAMS_ERROR, "文件格式错误");
        // 获取当前登录用户
        User loginUser = (User) StpUtil.getSession().get(USER_LOGIN_STATE);
        // todo 校验 galleryId 是否存在
        // 是否为公共图库
        boolean isPublic = CommonConstant.COMMON_GALLERY_ID.equals(galleryId);
        // 构造上传路径前缀
        String objectPrefix = isPublic ? "public/" + loginUser.getUserId() : "gallery/" + galleryId;
        // 构造原图上传路径 <gallery>/<galleryId>/2025-01-01-uuid.jpg
        String uuid = RandomUtil.randomString(16);
        String uploadFilename = String.format("%s-%s.%s", LocalDate.now(), uuid, format);
        String objectPath = String.format("%s/%s", objectPrefix, uploadFilename);
        // 调用下层服务
        return ossManage.getPresignedUrlForPut(objectPath, isPublic);
    }

    /**
     * 获取 STS 临时凭证
     */
    @Override
    public StsTokenVO getStsToken() {
        var credentials = stsManage.getStsToken();
        StsTokenVO vo = new StsTokenVO();
        vo.setAccessKeyId(credentials.getAccessKeyId());
        vo.setAccessKeySecret(credentials.getAccessKeySecret());
        vo.setSecurityToken(credentials.getSecurityToken());
        vo.setExpiration(credentials.getExpiration());
        return vo;
    }

    /**
     * 上传图片 （直传）
     */
    @Override
    public Long uploadPicture(PictureUploadDTO upload) {
        // 获取用户信息
        User loginUser = (User) StpUtil.getSession().get(USER_LOGIN_STATE);
        // todo 校验 galleryId 是否存在
        Long galleryId = upload.getGalleryId();
        // 是否为公共图库
        boolean isPublic = CommonConstant.COMMON_GALLERY_ID.equals(galleryId);
        // 调用下层服务
        return pictureService.uploadPicture(upload, loginUser, isPublic);
    }

    /**
     * 删除图片
     */
    @Override
    public boolean deletePicture(PictureDeleteDTO delete) {
        // todo 校验 galleryId 是否存在
        Long galleryId = delete.getGalleryId();
        return pictureService.deletePicture(delete.getPictureId());
    }

    /**
     * 更新图片
     */
    @Override
    public boolean updatePicture(PictureUpdateDTO update) {
        User loginUser = (User) StpUtil.getSession().get(USER_LOGIN_STATE);
        // todo 校验用户是否有权限访问该图片
        Long galleryId = update.getGalleryId();
        return pictureService.updatePicture(update, loginUser);
    }

    /**
     * 根据图片 id 获取图片信息 （用户）
     */
    @Override
    public PictureVO getPictureVOById(PictureQueryByIdDTO queryById) {
        User loginUser = (User) StpUtil.getSession().get(USER_LOGIN_STATE);
        // todo 校验用户是否有权限访问该图片
        Long galleryId = queryById.getGalleryId();
        return pictureService.getPictureVOById(queryById.getPictureId(), loginUser);
    }

    /**
     * 分页获取图片列表（用户）
     */
    @Override
    public Page<PictureVO> listPictureVOByPage(PictureQueryDTO query) {
        User loginUser = (User) StpUtil.getSession().get(USER_LOGIN_STATE);
        // todo 校验用户是否有权限访问该图片
        Long galleryId = query.getGalleryId();
        return pictureService.listPictureVOByPage(query);
    }
}
