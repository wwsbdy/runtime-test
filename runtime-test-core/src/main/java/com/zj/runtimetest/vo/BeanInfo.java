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

    private Class<?> cls;
    /**
     * bean示例，此处可能是代理类，不能直接bean.getClass()
     */
    private Object bean;
    private ClassLoader classLoader;

    public static BeanInfo empty() {
        return new BeanInfo(null, null, null);
    }
}
