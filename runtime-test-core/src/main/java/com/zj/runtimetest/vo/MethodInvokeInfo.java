package com.zj.runtimetest.vo;

import com.zj.runtimetest.exp.RuntimeTestExprExecutor;
import com.zj.runtimetest.utils.ClassUtil;
import com.zj.runtimetest.utils.FiledUtil;
import com.zj.runtimetest.utils.HttpServletRequestUtil;
import com.zj.runtimetest.utils.JsonUtil;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : jie.zhou
 * @date : 2025/7/1
 */
@Getter
public class MethodInvokeInfo {

    private final boolean staticMethod;
    private final String className;
    private final String methodName;
    private final ClassLoader classLoader;
    private final Object bean;
    private Class<?>[] paramClazzArr;
    private Method method;
    private final List<MethodParamTypeInfo> parameterTypeList;
    private boolean returnValue;
    private final String projectBasePath;

    public MethodInvokeInfo(RequestInfo requestInfo, ClassLoader classLoader, Object bean) {
        if (Objects.nonNull(requestInfo.getParameterTypeList()) && !requestInfo.getParameterTypeList().isEmpty()) {
            paramClazzArr = requestInfo.getParameterTypeList().stream().map(MethodParamInfo::getParamType).map(clsStr -> {
                        try {
                            if (clsStr.contains(".")) {
                                return ClassUtil.getClass(clsStr, classLoader);
                            }
                            // 兼容范型
                            return Object.class;
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(Class[]::new);
        }
        this.staticMethod = requestInfo.isStaticMethod();
        this.className = requestInfo.getClassName();
        this.methodName = requestInfo.getMethodName();
        this.projectBasePath = requestInfo.getProjectBasePath();
        this.parameterTypeList = Optional.ofNullable(requestInfo.getParameterTypeList())
                .map(list -> list.stream()
                        .map(v -> new MethodParamTypeInfo(v.getParamName(), v.getParamType(), null))
                        .collect(Collectors.toList())
                )
                .orElse(Collections.emptyList());
        this.classLoader = classLoader;
        this.bean = bean;
    }


    public Object invoke(ExpressionVo expVo, String requestJson) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        if (Objects.isNull(method)) {
            synchronized (this) {
                if (Objects.nonNull(method)) {
                    return method.invoke(bean, getArgs(expVo, requestJson));
                }
                if (staticMethod) {
                    method = ClassUtil.getClass(className, classLoader).getDeclaredMethod(methodName, paramClazzArr);
                } else {
                    method = bean.getClass().getDeclaredMethod(methodName, paramClazzArr);
                }
                method.setAccessible(true);
                returnValue = method.getReturnType() != void.class;
                return method.invoke(bean, getArgs(expVo, requestJson));
            }
        }
        return method.invoke(bean, getArgs(expVo, requestJson));
    }

    private Object[] getArgs(ExpressionVo expVo, String requestJson) {
        Type[] parameterTypes = method.getGenericParameterTypes();
        if (parameterTypes.length == 0) {
            return before(expVo, null, null);
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
        Object httpServletRequest = null;
        for (int i = 0; i < parameterTypes.length; i++) {
            MethodParamTypeInfo methodParamTypeInfo = parameterTypeList.get(i);
            Object arg = map.get(methodParamTypeInfo.getParamName());
            Type argType;
            if (Objects.isNull(methodParamTypeInfo.getType())) {
                argType = parameterTypes[i];
                methodParamTypeInfo.setType(argType);
            } else {
                argType = methodParamTypeInfo.getType();
            }
            if (HttpServletRequestUtil.isHttpServletRequest(paramClazzArr[i])) {
                args[i] = httpServletRequest =
                        HttpServletRequestUtil.setRequestAttributes(null, Objects.isNull(arg) ? null : JsonUtil.toMap(JsonUtil.toJsonString(arg)));
            } else if (arg == null) {
                args[i] = FiledUtil.getFieldNullValue(paramClazzArr[i]);
            } else {
                args[i] = JsonUtil.convertValue(arg, argType);
            }
        }
        return before(expVo, args, httpServletRequest);
    }

    private Object[] before(ExpressionVo expVo, Object[] args, Object httpServletRequest) {
        return RuntimeTestExprExecutor.evaluate(expVo, parameterTypeList, projectBasePath, args, httpServletRequest);
    }
}
