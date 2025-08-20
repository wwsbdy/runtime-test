package com.zj.runtimetest.utils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Spring AOP 代理工具类
 * @author : jie.zhou
 * @date : 2025/8/19
 */
public class ProxyUtil {
    private static final Set<String> PROXY_INTERFACE_NAMES = new HashSet<>(Collections.singletonList(
            "org.springframework.aop.framework.Advised"
    ));

    /** 判断类是否是 Spring AOP 代理 */
    public static boolean isProxy(Class<?> clazz) {
        if (clazz != null) {
            for (Class<?> itf : clazz.getInterfaces()) {
                if (PROXY_INTERFACE_NAMES.contains(itf.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取代理的原始对象：
     * - 如果是 Spring AOP 代理 → 递归 unwrap
     * - 否则原样返回
     */
    public static Object getProxiedInstance(Object candidate) {
        if (candidate == null) {
            return null;
        }
        Object current = candidate;
        // 最多解 16 层，避免死循环
        for (int i = 0; i < 16; i++) {
            if (!isProxy(current.getClass())) {
                // 不是 Spring 代理，直接返回
                return current;
            }
            Object target = unwrapOnce(current);
            if (target == null || target == current) {
                // 解不动了
                return current;
            }
            current = target;
        }
        return current;
    }

    /** 解一层 Spring AOP 代理（失败返回 null） */
    private static Object unwrapOnce(Object proxy) {
        try {
            Method getTargetSource = proxy.getClass().getMethod("getTargetSource");
            Object targetSource = getTargetSource.invoke(proxy);
            if (targetSource == null) {
                return null;
            }

            Method getTarget = targetSource.getClass().getMethod("getTarget");
            return getTarget.invoke(targetSource);
        } catch (Exception e) {
            // 解失败就返回 null，上层会保持原对象
            return null;
        }
    }
}
