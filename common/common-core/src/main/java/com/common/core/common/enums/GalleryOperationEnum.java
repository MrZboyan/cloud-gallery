package com.common.core.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum GalleryOperationEnum {

    VIEW("查看", 1),
    DELETE("删除", 1),
    UPDATE("更新", 1),
    UPLOAD("上传", 1),
    DOWNLOAD("下载", 1),
    // 有权限
    HAS_PERMISSION("有权限", 1),
    NO_PERMISSION("无权限", 0);

    private final String text;

    private final Integer value;

    GalleryOperationEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }


    /**
     * 根据 value 获取枚举
     */
    public static GalleryOperationEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (GalleryOperationEnum anEnum : GalleryOperationEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    /**
     * 获取值列表
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }
}
