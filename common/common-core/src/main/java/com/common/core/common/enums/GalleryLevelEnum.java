package com.common.core.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图库等级枚举
 */
@Getter
public enum GalleryLevelEnum {

    DEFAULT("默认", "default", 5L * 1024 * 1024 * 1024),

    MID("中级", "mid", 20L * 1024 * 1024 * 1024),

    HIGH("高级", "high", 100L * 1024 * 1024 * 1024),

    SUPER("至尊", "super", 1024L * 1024 * 1024 * 1024);

    private final String text;

    private final String value;

    private final long capacityBytes;

    GalleryLevelEnum(String text, String value, long capacityBytes) {
        this.text = text;
        this.value = value;
        this.capacityBytes = capacityBytes;
    }

    /**
     * 根据 value 获取当前等级对应容量
     */
    public static long getCapacityValue(String value) {
        for (GalleryLevelEnum anEnum : GalleryLevelEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum.capacityBytes;
            }
        }
        return -1L;
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
    public static GalleryLevelEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (GalleryLevelEnum anEnum : GalleryLevelEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
