package com.zj.runtimetest.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : jie.zhou
 * @date : 2025/6/30
 */
@Data
@AllArgsConstructor
public class CacheAndKeyVo {
    private String cacheKey;
    private CacheVo cache;
}
