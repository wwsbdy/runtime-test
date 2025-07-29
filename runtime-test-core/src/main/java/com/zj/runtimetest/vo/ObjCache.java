package com.zj.runtimetest.vo;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 缓存对象，先进先出
 *
 * @author 19242
 */
public class ObjCache<K, V> {

    private final LinkedHashMap<K, V> map;

    public ObjCache() {
        this(null);
    }

    /**
     * @param size 缓存大小
     */
    public ObjCache(Integer size) {
        map = new LinkedHashMap<K, V>(16, 0.75f, true) {
            private static final long serialVersionUID = 4250833893593797814L;

            /**
             * 当缓存大小超出时，会调用此方法，将前面的缓存项删除
             */
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                if (size == null) {
                    return false;
                }
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
        return !isEmpty();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public List<K> getKeys() {
        if (map.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<K> cacheKeyList = new ArrayList<>(map.keySet());
        Collections.reverse(cacheKeyList);
        return cacheKeyList;
    }

    public void foreach(BiConsumer<K, V> consumer) {
        for (K cacheKey : getKeys()) {
            consumer.accept(cacheKey, get(cacheKey));
        }
    }

    public V computeIfAbsent(K classLoader, Function<? super K, ? extends V> mappingFunction) {
        return map.computeIfAbsent(classLoader, mappingFunction);
    }
}
