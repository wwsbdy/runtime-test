package com.zj.runtimetest.test.cache;

import com.zj.runtimetest.test.vo.CacheVo;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : jie.zhou
 * @date : 2025/7/2
 */
@Data
public class CacheState {

    private Map<String, List<CacheVo>> cache = new ConcurrentHashMap<>();

    public void putCache(String key, CacheVo value) {
        if (Objects.isNull(value)) {
            return;
        }

    }

    public List<CacheVo> getCache(String key) {
        return cache.get(key);
    }



}
