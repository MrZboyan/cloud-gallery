package com.project.picture.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.core.common.annotation.AuthCheck;
import com.common.core.common.constant.UserConstant;
import com.common.core.common.result.BaseResponse;
import com.common.core.common.result.ResultUtils;
import com.common.core.model.picture.entity.Picture;
import com.common.core.model.picture.vo.PictureVO;
import com.project.picture.domain.PictureDomain;
import com.project.picture.model.dto.*;
import com.project.picture.model.vo.StsTokenVO;
import com.project.picture.service.PictureService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureDomain pictureDomain;

    @Resource
    private PictureService pictureService;

    /**
     * 上传图片（直传）
     */
    @PostMapping("/upload/direct")
    public BaseResponse<Long> uploadPicture(@RequestBody PictureUploadDTO upload) {
        return ResultUtils.success(pictureDomain.uploadPicture(upload));
    }

    /**
     * 获取图片访问 url
     */
    @GetMapping("/get/visit")
    public BaseResponse<String> getPresignedGetUrl(@RequestParam Long pictureId) {
        return ResultUtils.success(pictureDomain.getPresignedGetUrl(pictureId));
    }

    /**
     * 获取图片上传 url
     */
    @GetMapping("/get/put")
    public BaseResponse<String> getPresignedPutUrl(@RequestParam Long galleryId, @RequestParam String fileName) {
        return ResultUtils.success(pictureDomain.getPresignedPutUrl(galleryId, fileName));
    }

    /**
     * 获取 STS 临时凭证（前端直传用）
     */
    @GetMapping("/sts/token")
    public BaseResponse<StsTokenVO> getStsToken() {
        return ResultUtils.success(pictureDomain.getStsToken());
    }

    /**
     * 删除图片
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody PictureDeleteDTO delete) {
        return ResultUtils.success(pictureDomain.deletePicture(delete));
    }

    /**
     * 更新图片
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateDTO update) {
        return ResultUtils.success(pictureDomain.updatePicture(update));
    }

    /**
     * 根据图片 id 获取图片信息 （用户）
     */
    @GetMapping("/get/info")
    public BaseResponse<PictureVO> getPictureVOById(@RequestBody PictureQueryByIdDTO queryById) {
        return ResultUtils.success(pictureDomain.getPictureVOById(queryById));
    }

    /**
     * 分页获取图片列表（用户）
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryDTO query) {
        return ResultUtils.success(pictureDomain.listPictureVOByPage(query));
    }

    /**
     * 根据图片 id 获取图片信息 （管理员）
     */
    @GetMapping("/get/info/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(Long pictureId) {
        return ResultUtils.success(pictureService.getPictureById(pictureId));
    }

    /**
     * 分页获取图片列表（管理员）
     */
    @GetMapping("/list/page/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryDTO query) {
        return ResultUtils.success(pictureService.listPictureByPage(query));
    }

}