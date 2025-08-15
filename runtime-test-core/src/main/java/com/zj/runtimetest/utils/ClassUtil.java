package com.zj.runtimetest.utils;

import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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

    public static boolean isPrimitive(String className) {
        return PRIMITIVE_TYPE_MAP.containsKey(className);
    }


    /**
     * 将 Type 中的未绑定泛型 <T> 替换为 <Object>
     *
     * @param type 原始类型
     * @return 擦除后的类型
     */
    public static Type eraseGenericType(Type type) {
        if (type instanceof TypeVariable) {
            // 未绑定泛型变量（如 <T>）替换为 Object
            return Object.class;
        } else if (type instanceof ParameterizedTypeImpl) {
            // 处理参数化类型（如 List<T>）
            ParameterizedTypeImpl pt = (ParameterizedTypeImpl) type;
            Type[] actualTypeArgs = pt.getActualTypeArguments();
            Type[] newTypeArgs = new Type[actualTypeArgs.length];
            // 递归擦除每个类型参数
            for (int i = 0; i < actualTypeArgs.length; i++) {
                newTypeArgs[i] = eraseGenericType(actualTypeArgs[i]);
            }
            // 重建 ParameterizedType
            return ParameterizedTypeImpl.make(pt.getRawType(), newTypeArgs, pt.getOwnerType());
        } else if (type instanceof GenericArrayType) {
            // 处理泛型数组（如 T[]）
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Type erasedComponentType = eraseGenericType(componentType);
            return GenericArrayTypeImpl.make(erasedComponentType);
        }
        // 普通类（如 String）或基本类型直接返回
        return type;
    }
}
