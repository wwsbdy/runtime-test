package com.zj.runtimetest.utils;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : jie.zhou
 * @date : 2025/7/3
 */
public class ClassUtil {

    private static final Map<String, Class<?>> PRIMITIVE_TYPE_MAP = new HashMap<>();

    static {
        PRIMITIVE_TYPE_MAP.put("byte", byte.class);
        PRIMITIVE_TYPE_MAP.put("short", short.class);
        PRIMITIVE_TYPE_MAP.put("int", int.class);
        PRIMITIVE_TYPE_MAP.put("long", long.class);
        PRIMITIVE_TYPE_MAP.put("float", float.class);
        PRIMITIVE_TYPE_MAP.put("double", double.class);
        PRIMITIVE_TYPE_MAP.put("char", char.class);
        PRIMITIVE_TYPE_MAP.put("boolean", boolean.class);
    }

    public static Class<?> getClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> clazz = PRIMITIVE_TYPE_MAP.get(className);
        if (clazz != null) {
            return clazz;
        }
        if (className.endsWith("...")) {
            return Array.newInstance(getClass(className.substring(0, className.length() - 3), classLoader), 0).getClass();
        }
        if (className.endsWith("[]")) {
            return Array.newInstance(getClass(className.substring(0, className.length() - 2), classLoader), 0).getClass();
        }
        return Class.forName(className, true, classLoader);
    }
}
