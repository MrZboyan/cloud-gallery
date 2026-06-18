package com.common.core.common.converter;

import cn.hutool.json.JSONUtil;
import com.common.core.model.picture.entity.Picture;
import com.common.core.model.picture.vo.PictureVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PictureConverter {

    PictureConverter INSTANCE = Mappers.getMapper(PictureConverter.class);

    @Mapping(target = "tags", qualifiedByName = "listToJson")
    @Mapping(target = "category", qualifiedByName = "listToJson")
    Picture toEntity(PictureVO pictureVO);

    @Mapping(target = "tags", qualifiedByName = "jsonToList")
    @Mapping(target = "category", qualifiedByName = "jsonToList")
    PictureVO toVO(Picture picture);

    @Named("listToJson")
    default String listToJson(List<String> list) {
        return list != null ? JSONUtil.toJsonStr(list) : null;
    }

    @Named("jsonToList")
    default List<String> jsonToList(String json) {
        return json != null ? JSONUtil.toList(json, String.class) : null;
    }
}
