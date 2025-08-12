package com.zj.runtimetest.utils;

import java.util.Objects;

/**
 * @author : jie.zhou
 * @date : 2025/7/1
 */
public class FiledUtil {


    public static Object getFieldNullValue(Class<?> argClazz) {
        if (argClazz.isPrimitive()) {
            if (argClazz == int.class) {
                return 0;
            } else if (argClazz == byte.class) {
                return (byte) 0;
            } else if (argClazz == short.class) {
                return (short) 0;
            } else if (argClazz == long.class) {
                return 0L;
            } else if (argClazz == float.class) {
                return 0F;
            } else if (argClazz == double.class) {
                return 0D;
            } else if (argClazz == char.class) {
                return '\u0000';
            } else if (argClazz == boolean.class) {
                return false;
            }
        }
        return null;
    }

    public static Object getFieldNullValue(String jvmQualifiedClassName) {
        if (Objects.isNull(jvmQualifiedClassName) || jvmQualifiedClassName.isEmpty()) {
            return null;
        }
        if (ClassUtil.isPrimitive(jvmQualifiedClassName)) {
            if ("byte".equals(jvmQualifiedClassName)) {
                return 0;
            } else if ("short".equals(jvmQualifiedClassName)) {
                return (byte) 0;
            } else if ("int".equals(jvmQualifiedClassName)) {
                return (short) 0;
            } else if ("long".equals(jvmQualifiedClassName)) {
                return 0L;
            } else if ("float".equals(jvmQualifiedClassName)) {
                return 0F;
            } else if ("double".equals(jvmQualifiedClassName)) {
                return 0D;
            } else if ("char".equals(jvmQualifiedClassName)) {
                return '\u0000';
            } else if ("boolean".equals(jvmQualifiedClassName)) {
                return false;
            }
        }
        return null;
    }
}
