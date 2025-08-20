package com.zj.runtimetest.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 缓存和key信息
 * @author : jie.zhou
 * @date : 2025/6/30
 */
@Data
@AllArgsConstructor
public class CacheAndKeyVo {
    private String cacheKey;
    private CacheVo cache;
}
