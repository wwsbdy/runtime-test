package com.zj.runtimetest.vo;

import com.zj.runtimetest.utils.FiledUtil;
import com.zj.runtimetest.utils.JsonUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author : jie.zhou
 * @date : 2025/7/1
 */
public class MethodInvokeInfo {

    private final RequestInfo requestInfo;
    private final ClassLoader classLoader;
    private final Object bean;
    private Class<?>[] paramClazzArr;
    private Object[] args;

    public MethodInvokeInfo(RequestInfo requestInfo, ClassLoader classLoader, Object bean) {
        if (Objects.nonNull(requestInfo.getParameterTypeList()) && !requestInfo.getParameterTypeList().isEmpty()) {
            paramClazzArr = requestInfo.getParameterTypeList().stream().map(MethodParamInfo::getParamType).map(clsStr -> {
                        try {
                            return Class.forName(clsStr, true, classLoader);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(Class[]::new);
            args = getArgs(requestInfo.getRequestJson(), requestInfo.getParameterTypeList(), paramClazzArr);
        }
        this.requestInfo = requestInfo;
        this.classLoader = classLoader;
        this.bean = bean;
    }

    private Object[] getArgs(String content, List<MethodParamInfo> parameterTypeList, Class<?>[] paramClazzArr) {
        if (Objects.isNull(parameterTypeList) || parameterTypeList.isEmpty()
                || Objects.isNull(paramClazzArr) || paramClazzArr.length == 0
                || paramClazzArr.length != parameterTypeList.size()) {
            return null;
        }
        Map<String, Object> map;
        if (Objects.isNull(content) || content.isEmpty()) {
            map = Collections.emptyMap();
        } else {
            map = JsonUtil.toMap(content);
        }
        Object[] args = new Object[parameterTypeList.size()];
        for (int i = 0; i < paramClazzArr.length; i++) {
            Object arg = map.get(parameterTypeList.get(i).getParamName());
            Class<?> argClazz = paramClazzArr[i];
            if (arg == null) {
                args[i] = FiledUtil.getFieldNullValue(argClazz);
                continue;
            }
            args[i] = JsonUtil.convertValue(arg, paramClazzArr[i]);
        }
        return args;
    }


    public Object invoke() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        boolean staticMethod = requestInfo.isStaticMethod();
        String className = requestInfo.getClassName();
        String methodName = requestInfo.getMethodName();
        Method method;
        if (staticMethod) {
            method = Class.forName(className, true, classLoader).getDeclaredMethod(methodName, paramClazzArr);
        } else {
            method = bean.getClass().getDeclaredMethod(methodName, paramClazzArr);
        }
        method.setAccessible(true);
        return method.invoke(bean, args);
    }
}
