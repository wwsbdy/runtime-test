package com.zj.runtimetest.vo;

import com.zj.runtimetest.exp.RuntimeTestExprExecutor;
import com.zj.runtimetest.utils.*;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author : jie.zhou
 * @date : 2025/7/1
 */
@Getter
public class MethodInvokeInfo {

    private final Method method;
    private final List<MethodParamTypeInfo> parameterList;
    private final boolean returnValue;
    private final BeanInfo beanInfo;
    private final Object bean;
    private final boolean privateMethodProxyClass;

    public MethodInvokeInfo(RequestInfo requestInfo, BeanInfo beanInfo) throws NoSuchMethodException, ClassNotFoundException {
        this.beanInfo = beanInfo;
        List<MethodParamInfo> parameterTypeList = requestInfo.getParameterTypeList();
        Class<?>[] paramClazzArr = null;
        // 封装入参类型
        if (Objects.nonNull(parameterTypeList) && !parameterTypeList.isEmpty()) {
            paramClazzArr = new Class[parameterTypeList.size()];
            this.parameterList = new ArrayList<>();
            for (int i = 0; i < parameterTypeList.size(); i++) {
                MethodParamInfo methodParamInfo = parameterTypeList.get(i);
                String clsStr = methodParamInfo.getParamType();
                if (ClassUtil.isPrimitive(clsStr) || clsStr.contains(".")) {
                    paramClazzArr[i] = ClassUtil.getClass(clsStr, beanInfo.getClassLoader());
                } else {
                    // 兼容范型
                    paramClazzArr[i] = Object.class;
                }
                this.parameterList.add(new MethodParamTypeInfo(methodParamInfo.getParamName(), methodParamInfo.getParamType(), paramClazzArr[i]));
            }
        } else {
            this.parameterList = Collections.emptyList();
        }
        method = beanInfo.getCls().getDeclaredMethod(requestInfo.getMethodName(), paramClazzArr);
        method.setAccessible(true);
        returnValue = method.getReturnType() != void.class;
        // 兼容代理
        privateMethodProxyClass = Objects.nonNull(beanInfo.getBean())
                && !beanInfo.getCls().equals(beanInfo.getBean().getClass())
                && !Modifier.isPublic(method.getModifiers())
                && ProxyUtil.isProxy(beanInfo.getBean().getClass());
        if (privateMethodProxyClass) {
            bean = ProxyUtil.getProxiedInstance(beanInfo.getBean());
        } else {
            bean = beanInfo.getBean();
        }
        // 赋值type
        Type[] parameterTypes = method.getGenericParameterTypes();
        if (parameterTypes.length > 0) {
            for (int i = 0; i < parameterTypes.length; i++) {
                MethodParamTypeInfo methodParamTypeInfo = this.parameterList.get(i);
                methodParamTypeInfo.setType(ClassUtil.eraseGenericType(parameterTypes[i]));
            }
        }
    }

    public Object invoke(ExpressionVo expVo, String requestJson) throws InvocationTargetException, IllegalAccessException {
        if (privateMethodProxyClass) {
            LogUtil.log("[Agent more] method is private, try to get proxied instance");
        }
        return method.invoke(bean, getArgs(expVo, requestJson));
    }

    private Object[] getArgs(ExpressionVo expVo, String requestJson) {
        if (parameterList.isEmpty()) {
            LogUtil.log("[Agent more] method no params");
            return before(expVo, null);
        }
        Map<String, Object> map;
        if (Objects.isNull(requestJson) || requestJson.isEmpty()) {
            LogUtil.log("[Agent more] requestJson is empty");
            map = Collections.emptyMap();
        } else {
            map = JsonUtil.toMap(requestJson);
        }
        Object[] args = new Object[parameterList.size()];
        for (int i = 0; i < parameterList.size(); i++) {
            MethodParamTypeInfo methodParamTypeInfo = parameterList.get(i);
            Object arg = map.get(methodParamTypeInfo.getParamName());
            if (HttpServletRequestUtil.isHttpServletRequest(methodParamTypeInfo.getCls())) {
                IHttpServletRequest httpServletRequest = HttpServletRequestUtil.getHttpServletRequest();
                args[i] = httpServletRequest;
                if (Objects.nonNull(httpServletRequest) && Objects.nonNull(arg)) {
                    Map<String, Object> headers = JsonUtil.toMap(JsonUtil.toJsonString(arg));
                    headers.forEach(httpServletRequest::addHeader);
                }
            } else if (Objects.isNull(arg)) {
                args[i] = FiledUtil.getFieldNullValue(methodParamTypeInfo.getCls());
            } else {
                args[i] = JsonUtil.convertValue(arg, methodParamTypeInfo.getType());
            }
        }
        return before(expVo, args);
    }

    private Object[] before(ExpressionVo expVo, Object[] args) {
        return RuntimeTestExprExecutor.evaluate(expVo, parameterList, args);
    }
}
