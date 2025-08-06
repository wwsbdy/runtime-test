package com.zj.runtimetest.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 常量
 *
 * @author 19242
 */
public interface Constant {
    /**
     * 实体类转json 最大递归层级，防止死循环
     */
    int MAX_RECURSION_LEVEL = 2;

    /**
     * 历史请求json记录最大长度
     */
    int HISTORY_SIZE = 5;

    /**
     * 旧工具栏的缓存key
     */
    String TOOLWINDOW_CONTENT_CACHE_KEY = "bfa86f7f-d1e1-5b25-d32f-53cf6031f29f";
    String TOOLWINDOW_CONTENT_CACHE_KEY_2 = "42967483-ca8b-44cf-b6eb-dbed6d762e19";
    String TOOLWINDOW_CONTENT_CACHE_KEY_3 = "d6c78aeb-7a2f-491d-9910-8ef2af5e520a";
    String TOOLWINDOW_CONTENT_CACHE_KEY_4 = "582e4a3e-3008-4be7-86e0-a103fa7f6ec3";
    String TOOLWINDOW_CONTENT_CACHE_KEY_5 = "e57ee9bf-bf41-41d2-b5a3-f39080ed0773";
    List<String> TOOLWINDOW_CONTENT_CACHE_KEY_LIST = Arrays.asList(TOOLWINDOW_CONTENT_CACHE_KEY,
            TOOLWINDOW_CONTENT_CACHE_KEY_2,
            TOOLWINDOW_CONTENT_CACHE_KEY_3,
            TOOLWINDOW_CONTENT_CACHE_KEY_4,
            TOOLWINDOW_CONTENT_CACHE_KEY_5);

    /**
     * 最大标签页长度
     */
    int TAB_MAX = TOOLWINDOW_CONTENT_CACHE_KEY_LIST.size();
}
