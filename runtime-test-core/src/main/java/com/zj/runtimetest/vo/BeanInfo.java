package com.zj.runtimetest.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : jie.zhou
 * @date : 2025/6/30
 */
@Data
@AllArgsConstructor
public class BeanInfo {
    private String className;
    private Object bean;
    private ClassLoader classLoader;

    public static BeanInfo empty() {
        return new BeanInfo(null, null, null);
    }
}
