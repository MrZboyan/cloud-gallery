package com.common.web.utils;

/**
 * 请求 ID 上下文
 */
public class RequestIdContext {
    
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    
    public static void set(String requestId) {
        REQUEST_ID.set(requestId);
    }
    
    public static String get() {
        return REQUEST_ID.get();
    }
    
    public static void remove() {
        REQUEST_ID.remove();
    }
}