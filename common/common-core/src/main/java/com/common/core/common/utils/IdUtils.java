package com.common.core.common.utils;

/**
 * id 工具类
 */
public class IdUtils {

    /**
     * 校验 id
     */
    public static boolean checkId(Long id) {
        return id == null || id <= 0;
    }

}