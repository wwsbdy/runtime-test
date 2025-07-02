package com.zj.runtimetest.vo;

import java.util.LinkedHashMap;

/**
 * @author 19242
 */
public class BeanCache {

    private final int size;
    private final LinkedHashMap<String, BeanInfo> map = new LinkedHashMap<>();

    public BeanCache(int size) {
        this.size = size;
    }

    public void put(String className, BeanInfo bean) {
        if (map.containsKey(className)) {
            map.remove(className);
            map.put(className, bean);
            return;
        }
        if (map.size() >= size) {
            map.remove(map.keySet().iterator().next());
        }
        map.put(className, bean);
    }

    public BeanInfo get(String className) {
        return map.get(className);
    }

    public BeanInfo remove(String className) {
        return map.remove(className);
    }

    public boolean isNotEmpty() {
        return !map.isEmpty();
    }
}
