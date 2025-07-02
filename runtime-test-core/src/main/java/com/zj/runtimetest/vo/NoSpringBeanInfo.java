package com.zj.runtimetest.vo;

import com.zj.runtimetest.utils.FiledUtil;

import java.lang.reflect.Constructor;

/**
 * @author : jie.zhou
 * @date : 2025/7/1
 */
public class NoSpringBeanInfo extends BeanInfo {

    private boolean isInit = false;

    public NoSpringBeanInfo(String className, ClassLoader classLoader) {
        super(className, null, classLoader);
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
                    Object instanceSmart = createInstanceSmart(Class.forName(super.getClassName(), true, super.getClassLoader()));
                    super.setBean(instanceSmart);
                    return instanceSmart;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("[Agent] Create bean by constructor fail: " + super.getClassName());
                }
            }
        }
        return super.getBean();
    }

    public Object createInstanceSmart(Class<?> clazz) throws Exception {
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
