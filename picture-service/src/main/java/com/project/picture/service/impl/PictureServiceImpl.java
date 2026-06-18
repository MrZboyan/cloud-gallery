package com.project.picture.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.aliyun.oss.model.ObjectMetadata;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.common.constant.CommonConstant;
import com.common.core.common.converter.PictureConverter;
import com.common.core.common.converter.UserConverter;
import com.common.core.common.enums.ReviewStatusEnum;
import com.common.core.common.enums.UserRoleEnum;
import com.common.core.common.utils.IdUtils;
import com.common.core.exception.BusinessException;
import com.common.core.exception.ErrorCode;
import com.common.core.exception.ThrowUtils;
import com.common.core.model.picture.entity.Picture;
import com.common.core.model.picture.vo.PictureVO;
import com.common.core.model.user.entity.User;
import com.common.core.model.user.vo.UserVO;
import com.project.picture.model.dto.PictureQueryDTO;
import com.project.picture.model.dto.PictureUpdateDTO;
import com.project.picture.model.dto.PictureUploadDTO;
import com.project.picture.model.mapper.PictureMapper;
import com.project.picture.service.PictureService;
import com.project.picture.upload.OssManage;
import com.project.picture.utils.ImageUtils;
import com.user.api.service.UserRpcService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.common.core.common.constant.CommonConstant.MAX_PICTURE_SIZE;

@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private OssManage ossManage;

    @DubboReference(version = "1.0.0")
    private UserRpcService userRpcService;

    /**
     * 上传图片（直传）
     */
    @Override
    public Long uploadPicture(PictureUploadDTO upload, User loginUser, boolean isPublic) {
        String objectPath = upload.getObjectPath();
        // 从 OSS 获取上传的图片信息
        ObjectMetadata info = ossManage.getObjectMeta(objectPath, isPublic);
        // 校验
        ThrowUtils.throwIf(info == null, ErrorCode.OPERATION_ERROR, "图片不存在！");
        ThrowUtils.throwIf(info.getContentLength() > MAX_PICTURE_SIZE, ErrorCode.OPERATION_ERROR, "图片过大");
        // 获取图片
        ImageUtils.ImageMeta meta = ossManage.getObjectMetaAndParseImage(objectPath, isPublic);
        // 构造 picture 实体类
        Picture picture = this.buildPicture(upload, loginUser, meta, objectPath);
        // 设置审核状态
        this.buildReview(picture, loginUser);
        // 入库
        boolean save = this.save(picture);
        ThrowUtils.throwIf(!save, ErrorCode.DATABASE_ERROR);
        return picture.getPictureId();
    }

    /**
     * 删除图片
     */
    @Override
    public boolean deletePicture(Long pictureId) {
        // 操作数据库
        boolean delete = this.removeById(pictureId);
        ThrowUtils.throwIf(!delete, ErrorCode.DATABASE_ERROR, "数据库异常，删除失败！");
        return true;
    }

    /**
     * 更新图片
     */
    @Override
    public boolean updatePicture(PictureUpdateDTO update, User loginUser) {
        // 参数校验
        Picture picture = this.getOne(new QueryWrapper<Picture>().eq("pictureId", update.getPictureId()));
        BeanUtils.copyProperties(update, picture);
        // 类型不同，需要转换
        picture.setCategory(JSONUtil.toJsonStr(update.getCategory()));
        picture.setTags(JSONUtil.toJsonStr(update.getTags()));
        // 执行更新操作
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.DATABASE_ERROR, "数据库异常，更新失败！");
        return true;
    }

    /**
     * 根据图片 id 获取图片 （管理员）
     */
    @Override
    public Picture getPictureById(Long pictureId) {
        Picture picture = this.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "未找到该图片！");
        return picture;
    }

    /**
     * 根据图片 id 获取图片 （用户）
     */
    @Override
    public PictureVO getPictureVOById(Long pictureId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(IdUtils.checkId(pictureId), ErrorCode.PARAMS_ERROR, "图片 ID 有误！");
        // 图片是否存在 并且是过审的
        Picture picture = this.getOne(new QueryWrapper<Picture>().eq("pictureId", pictureId));
        ThrowUtils.throwIf(ObjectUtils.isEmpty(picture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        Integer reviewStatus = picture.getReviewStatus();
        if (!ReviewStatusEnum.PASS.equals(ReviewStatusEnum.getEnumByValue(reviewStatus))) {
            // 非过审图片 仅管理员可见
            if (!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, picture.getReviewMessage());
            }
        }
        PictureVO pictureVO = PictureConverter.INSTANCE.toVO(picture);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(pictureVO), ErrorCode.NOT_FOUND_ERROR, "未找到该图片！");
        // 填充用户信息
        Long userId = picture.getUserId();
        User user = userRpcService.getUserById(userId);
        pictureVO.setUserVO(UserConverter.INSTANCE.toVO(user));
        return pictureVO;
    }

    /**
     * 分页获取图片列表（管理员）
     */
    @Override
    public Page<Picture> listPictureByPage(PictureQueryDTO query) {
        // 构造分页参数
        int current = query.getCurrent();
        int pageSize = query.getPageSize();
        ThrowUtils.throwIf(pageSize >= 50, ErrorCode.PARAMS_ERROR);
        // 返回数据
        return this.page(new Page<>(current, pageSize), this.getQueryWrapper(query));
    }

    /**
     * 分页获取图片列表（用户）
     */
    @Override
    public Page<PictureVO> listPictureVOByPage(PictureQueryDTO query) {
        // 构造分页参数
        int current = query.getCurrent();
        int pageSize = query.getPageSize();
        ThrowUtils.throwIf(pageSize >= 50, ErrorCode.PARAMS_ERROR);
        // 用户只能看到已经过审的图片
        QueryWrapper<Picture> queryWrapper = this.getQueryWrapper(query);
        queryWrapper.eq("reviewStatus", ReviewStatusEnum.PASS.getValue());
        // 获取数据
        Page<Picture> picturePage = this.page(new Page<>(current, pageSize), queryWrapper);
        List<Picture> pictureList = picturePage.getRecords();
        if (CollectionUtils.isEmpty(pictureList)) {
            return new Page<>();
        }
        // 转换为 VO
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureConverter.INSTANCE::toVO).toList();
        // 填充用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userRpcService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.toMap(User::getUserId, UserConverter.INSTANCE::toVO));
        // 填充到分页数据中
        List<PictureVO> pictureVOS = pictureVOList.stream()
                .peek(pictureVO -> pictureVO.setUserVO(userVOMap.get(pictureVO.getUserId()))).toList();
        Page<PictureVO> pictureVOPage = new Page<>(current, pageSize, picturePage.getTotal());
        pictureVOPage.setRecords(pictureVOS);
        return pictureVOPage;
    }

    /**
     * 获取查询条件
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryDTO query) {
        // todo 可以优化拆分出其他接口 例如按照时间查询 单独写 SQL
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (query == null) {
            return queryWrapper;
        }
        Long pictureId = query.getPictureId();
        Long userId = query.getUserId();
        Long galleryId = query.getGalleryId();
        String name = query.getName();
        String introduction = query.getIntroduction();
        List<String> category = query.getCategory();
        List<String> tags = query.getTags();
        Long picSize = query.getPicSize();
        Integer picWidth = query.getPicWidth();
        Integer picHeight = query.getPicHeight();
        Double picScale = query.getPicScale();
        String picFormat = query.getPicFormat();
        String picColor = query.getPicColor();
        Integer reviewStatus = query.getReviewStatus();
        String reviewMessage = query.getReviewMessage();
        Long reviewerId = query.getReviewerId();
        LocalDateTime reviewTime = query.getReviewTime();
        LocalDateTime createTime = query.getCreateTime();
        LocalDateTime updateTime = query.getUpdateTime();
        String sortField = query.getSortField();
        String sortOrder = query.getSortOrder();
        // 精确匹配
        queryWrapper.eq(ObjectUtils.isNotEmpty(pictureId), "pictureId", pictureId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(galleryId), "galleryId", galleryId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjectUtils.isNotEmpty(picWidth), "picSize", picWidth);
        queryWrapper.eq(ObjectUtils.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjectUtils.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjectUtils.isNotEmpty(picFormat), "picFormat", picFormat);
        queryWrapper.eq(ObjectUtils.isNotEmpty(picColor), "picColor", picColor);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewTime), "reviewTime", reviewTime);
        queryWrapper.eq(ObjectUtils.isNotEmpty(createTime), "createTime", createTime);
        queryWrapper.eq(ObjectUtils.isNotEmpty(updateTime), "updateTime", updateTime);
        // 模糊匹配
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.like(category != null && !category.isEmpty(), "category", JSONUtil.toJsonStr(category));
        queryWrapper.like(tags != null && !tags.isEmpty(), "tags", JSONUtil.toJsonStr(tags));
        // 排序
        queryWrapper.orderBy(ObjectUtils.isNotEmpty(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_DESC),
                sortField);
        return queryWrapper;
    }

    /**
     * 构建图片入库信息
     */
    private Picture buildPicture(PictureUploadDTO upload, User loginUser, ImageUtils.ImageMeta meta,
                                 String filePathName) {
        Picture picture = new Picture();
        picture.setGalleryId(upload.getGalleryId());
        picture.setUserId(loginUser.getUserId());
        picture.setUrl(filePathName);
        picture.setName(upload.getName());
        picture.setIntroduction(upload.getIntroduction());
        picture.setCategory(JSON.toJSONString(upload.getCategory()));
        picture.setTags(JSON.toJSONString(upload.getTags()));
        picture.setPicSize(meta.getPicSize());
        picture.setPicWidth(meta.getPicWidth());
        picture.setPicHeight(meta.getPicHeight());
        picture.setPicScale(meta.getPicScale());
        picture.setPicFormat(meta.getPicFormat());
        picture.setPicColor(meta.getPicColor());
        picture.setThumbnailUrl(null);
        picture.setCreateTime(LocalDateTime.now());
        picture.setUpdateTime(LocalDateTime.now());
        return picture;
    }

    /**
     * 构建图片审核信息
     */
    private void buildReview(Picture picture, User loginUser) {
        if (loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())) {
            picture.setReviewStatus(ReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员上传，自动过审。");
            picture.setReviewerId(loginUser.getUserId());
            picture.setReviewTime(LocalDateTime.now());
        } else {
            picture.setReviewStatus(ReviewStatusEnum.REVIEWING.getValue());
            picture.setReviewMessage("等待管理员审核。");
            picture.setReviewerId(null);
            picture.setReviewTime(LocalDateTime.now());
        }
    }

}
