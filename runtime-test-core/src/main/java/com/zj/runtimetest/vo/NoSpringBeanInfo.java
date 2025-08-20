package com.zj.runtimetest.vo;

import com.zj.runtimetest.utils.ClassUtil;
import com.zj.runtimetest.utils.FiledUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * 创建非 spring bean
 * @author : jie.zhou
 * @date : 2025/7/1
 */
public class NoSpringBeanInfo extends BeanInfo {

    private boolean isInit = false;

    public NoSpringBeanInfo(String className, ClassLoader classLoader) throws ClassNotFoundException {
        super(ClassUtil.getClass(className, classLoader), null, classLoader);
    }

    @Override
    public Object getBean() {
        if (!isInit) {
            synchronized (this) {
                if (isInit) {
                    return super.getBean();
                }
                isInit = true;
                try {
                    Object instanceSmart = createInstanceSmart(getCls());
                    super.setBean(instanceSmart);
                    return instanceSmart;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return super.getBean();
    }

    public Object createInstanceSmart(Class<?> clazz) throws Exception {
        if (clazz.isPrimitive()) {
            throw new InstantiationException("Primitive type can not create instance");
        }
        if (clazz.isInterface()) {
            throw new InstantiationException("Interface can not create instance");
        }
        if (clazz.isArray()) {
            throw new InstantiationException("Array type can not create instance");
        }
        if (clazz.isAnnotation()) {
            throw new InstantiationException("Annotation can not create instance");
        }
        if (Modifier.isAbstract(clazz.getModifiers())) {
            throw new InstantiationException("Abstract type can not create instance");
        }
        if (clazz.isEnum()) {
            Object[] enumConstants = clazz.getEnumConstants();
            if (Objects.isNull(enumConstants) || enumConstants.length == 0) {
                throw new InstantiationException("Enum type can not create instance");
            }
            return clazz.getEnumConstants()[0];
        }
        Constructor<?> bestConstructor = getConstructor(clazz);
        // 构造参数
        Class<?>[] paramTypes = bestConstructor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = FiledUtil.getFieldNullValue(paramTypes[i]);
        }

        // 如果是 private 构造，需要设置可访问
        if (!bestConstructor.isAccessible()) {
            bestConstructor.setAccessible(true);
        }

        return bestConstructor.newInstance(args);
    }

    private Constructor<?> getConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        if (constructors.length == 0) {
            constructors = clazz.getDeclaredConstructors();
            if (constructors.length == 0) {
                throw new IllegalStateException("No any constructor");
            }
        }

        // 按参数数量排序，选择参数最少的构造方法
        Constructor<?> bestConstructor = constructors[0];
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() < bestConstructor.getParameterCount()) {
                bestConstructor = constructor;
            }
        }
        return bestConstructor;
    }
}
