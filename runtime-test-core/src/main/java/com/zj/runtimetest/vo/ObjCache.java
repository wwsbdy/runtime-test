package com.zj.runtimetest.vo;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * 缓存对象，先进先出
 * @author 19242
 */
public class ObjCache<K, V> {

    private final LinkedHashMap<K, V> map;

    /**
     * @param size 缓存大小
     */
    public ObjCache(int size) {
        map = new LinkedHashMap<K, V>(16, 0.75f, true) {
            private static final long serialVersionUID = 4250833893593797814L;

            /**
             * 当缓存大小超出时，会调用此方法，将前面的缓存项删除
             */
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
                return size() > size;
            }
        };
    }

    public void put(K key, V value) {
        // accessOrder=true 表示最近访问的元素会排在最后
        if (Objects.nonNull(map.get(key))) {
            return;
        }
        map.put(key, value);
    }

    public V get(K key) {
        return map.get(key);
    }

    public V remove(K key) {
        return map.remove(key);
    }

    public boolean isNotEmpty() {
        return !map.isEmpty();
    }
}
