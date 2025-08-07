package com.zj.runtimetest.utils;

import com.zj.runtimetest.exp.ExpressionExecutor;
import com.zj.runtimetest.exp.RuntimeTestExprExecutor;
import com.zj.runtimetest.vo.MethodParamInfo;
import com.zj.runtimetest.vo.MethodParamTypeInfo;
import com.zj.runtimetest.vo.RequestInfo;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author : jie.zhou
 * @date : 2025/7/4
 */
public class AgentUtil {

    public static boolean end;

    public static void run(RequestInfo requestInfo) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
        long startTime = System.currentTimeMillis();
        // 1. 获取当前 JVM 的 PID
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        String agentJarPath = System.getProperty("user.dir").replace("runtime-test-core", "") + "dist" + File.separator + "runtime-test-core.jar";

        String agentArgs = JsonUtil.toJsonString(requestInfo);
        // 3. 动态加载 Agent
        Class<?> aClass = Class.forName("com.sun.tools.attach.VirtualMachine");
        Method attach = aClass.getMethod("attach", String.class);
        attach.setAccessible(true);
        Object vm = attach.invoke(null, pid);
        aClass.getMethod("loadAgent", String.class, String.class).invoke(vm, agentJarPath, Base64Util.encode(agentArgs));
        aClass.getMethod("detach").invoke(vm);
    }

    public static void run(String className,
                           String methodName,
                           List<MethodParamInfo> parameterTypeList,
                           String requestJson) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setClassName(className);
        requestInfo.setMethodName(methodName);
        requestInfo.setParameterTypeList(parameterTypeList);
        requestInfo.setRequestJson(requestJson);
        run(requestInfo);
    }

    public static void run(Method method, String requestJson, MethodParamInfo... parameterTypes) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setClassName(method.getDeclaringClass().getName());
        requestInfo.setMethodName(method.getName());
        requestInfo.setParameterTypeList(
                Optional.ofNullable(parameterTypes)
                        .map(Stream::of)
                        .map(stream -> stream.collect(Collectors.toList()))
                        .orElse(null)
        );
        requestInfo.setRequestJson(requestJson);
        run(requestInfo);
    }

    public static ExpressionExecutor getExecutor(Method method, RequestInfo requestInfo) throws Exception {
        Type[] parameterTypes = method.getGenericParameterTypes();
        List<MethodParamInfo> parameterList = requestInfo.getParameterTypeList();
        List<MethodParamTypeInfo> parameterTypeList = new ArrayList<>();
        for (int i = 0; i < parameterTypes.length; i++) {
            MethodParamInfo methodParamInfo = parameterList.get(i);
            Type argType = parameterTypes[i];
            parameterTypeList.add(new MethodParamTypeInfo(methodParamInfo.getParamName(), methodParamInfo.getParamType(), argType));
        }
        return RuntimeTestExprExecutor.getExecutor(requestInfo.getExpVo(), parameterTypeList, requestInfo.getProjectBasePath());
    }

}
