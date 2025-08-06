package com.zj.runtimetest.constant;

/**
 * 常量
 *
 * @author 19242
 */
public interface Constant {
    /**
     * 最大标签页长度
     */
    int TAB_MAX = 5;
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
    String KEY = "bfa86f7f-d1e1-5b25-d32f-53cf6031f29f";
}
