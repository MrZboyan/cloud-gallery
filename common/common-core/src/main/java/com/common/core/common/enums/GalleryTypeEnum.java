package com.common.core.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图库类型枚举
 */
@Getter
public enum GalleryTypeEnum {

    COMMON("公共", "public"),
    SHARE("共享", "share"),
    TEAM("团队", "team"),
    PRIVATE("私人", "private");

    private final String text;

    private final String value;

    GalleryTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     */
    public static GalleryTypeEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (GalleryTypeEnum anEnum : GalleryTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
