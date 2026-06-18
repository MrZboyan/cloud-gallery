package com.common.core.common.utils;

import java.util.Set;

public class PicFormatUtils {

    /**
     * 支持的图片格式列表
     */
    private static final Set<String> FORMATS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");

    /**
     * 验证图片格式
     */
    public static boolean verifyFormat(String format) {
        return FORMATS.contains(format);
    }

    /**
     * 获取图片格式
     */
    public static String detectFormat(String name) {
        if (name == null) return null;
        int idx = name.lastIndexOf('.');
        if (idx == -1) return null;
        return name.substring(idx + 1).toLowerCase();
    }

    /**
     * 从 URL 中解析出 gallery <gallery>/<galleryId>/2025-01-01-uuid.jpg
     */
    public static String parseGalleryId(String url) {
        int idx = url.lastIndexOf('/');
        return url.substring(idx + 1, url.lastIndexOf('.'));
    }
}
