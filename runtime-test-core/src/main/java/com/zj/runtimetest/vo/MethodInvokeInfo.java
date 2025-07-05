package com.zj.runtimetest.vo;

import com.zj.runtimetest.utils.ClassUtil;
import com.zj.runtimetest.utils.FiledUtil;
import com.zj.runtimetest.utils.JsonUtil;
import lombok.Getter;

import java.lang.reflect.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author : jie.zhou
 * @date : 2025/7/1
 */
public class MethodInvokeInfo {

    private final boolean staticMethod;
    private final String className;
    private final String methodName;
    private final ClassLoader classLoader;
    private final Object bean;
    private Class<?>[] paramClazzArr;
    private Method method;
    private final List<MethodParamInfo> parameterTypeList;
    @Getter
    private boolean returnValue;

    public MethodInvokeInfo(RequestInfo requestInfo, ClassLoader classLoader, Object bean) {
        if (Objects.nonNull(requestInfo.getParameterTypeList()) && !requestInfo.getParameterTypeList().isEmpty()) {
            paramClazzArr = requestInfo.getParameterTypeList().stream().map(MethodParamInfo::getParamType).map(clsStr -> {
                        try {
                            return ClassUtil.getClass(clsStr, classLoader);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(Class[]::new);
        }
        this.staticMethod = requestInfo.isStaticMethod();
        this.className = requestInfo.getClassName();
        this.methodName = requestInfo.getMethodName();
        this.parameterTypeList = requestInfo.getParameterTypeList();
        this.classLoader = classLoader;
        this.bean = bean;
    }



    public Object invoke(String requestJson) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        if (Objects.isNull(method)) {
            synchronized (this) {
                if (Objects.nonNull(method)) {
                    return method.invoke(bean, getArgs(method, requestJson));
                }
                if (staticMethod) {
                    method = ClassUtil.getClass(className, classLoader).getDeclaredMethod(methodName, paramClazzArr);
                } else {
                    method = bean.getClass().getDeclaredMethod(methodName, paramClazzArr);
                }
                method.setAccessible(true);
                returnValue = method.getReturnType() != void.class;
                return method.invoke(bean, getArgs(method, requestJson));
            }
        }
        return method.invoke(bean, getArgs(method, requestJson));
    }

    private Object[] getArgs(Method method, String requestJson) {
        Type[] parameterTypes = method.getGenericParameterTypes();
        if (parameterTypes.length == 0) {
            return null;
        }
        if (parameterTypeList.size() != parameterTypes.length
                && parameterTypeList.size() != paramClazzArr.length) {
            throw new RuntimeException("method params size different");
        }
        Map<String, Object> map;
        if (Objects.isNull(requestJson) || requestJson.isEmpty()) {
            map = Collections.emptyMap();
        } else {
            map = JsonUtil.toMap(requestJson);
        }
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object arg = map.get(parameterTypeList.get(i).getParamName());
            Type argType = parameterTypes[i];
            if (arg == null) {
                args[i] = FiledUtil.getFieldNullValue(paramClazzArr[i]);
                continue;
            }
            args[i] = JsonUtil.convertValue(arg, argType);
        }
        return args;
    }
}
