package com.common.core.common.constant;

/**
 * 通用常量
 */
public interface CommonConstant {

    String REQUEST_ID = "request_id";

    String DUBBO_EXCEPTION_CODE = "biz_code";

    String DUBBO_EXCEPTION_MSG = "biz_msg";

    long MAX_PICTURE_SIZE = 1024 * 1024 * 10L;

    String THUMBNAIL_FORMAT = ".jpg";

    /**
     * 公共图库 id
     */
    Long COMMON_GALLERY_ID = 1L;

    /**
     * 无团队 id
     */
    Long NO_TEAM_ID = 0L;

    /**
     * 升序
     */
    String SORT_ORDER_ASC = "ascend";

    /**
     * 降序
     */
    String SORT_ORDER_DESC = " descend";

}