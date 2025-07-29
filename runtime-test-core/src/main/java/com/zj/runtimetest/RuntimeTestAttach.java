package com.zj.runtimetest;

import com.zj.runtimetest.utils.Base64Util;
import com.zj.runtimetest.utils.IOUtil;
import com.zj.runtimetest.utils.JsonUtil;
import com.zj.runtimetest.utils.ThrowUtil;
import com.zj.runtimetest.vo.RequestInfo;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author : jie.zhou
 * @date : 2025/6/25
 */
public class RuntimeTestAttach {

    /**
     * premain方法在JVM启动时执行，在main方法之前执行
     * 监听ApplicationContext，拦截ApplicationContext#refresh()方法，获取到spring上下文
     *
     * @param args main方法的参数
     * @param inst 仪表
     */
    public static void premain(String args, Instrumentation inst) {
        System.out.println("[Agent] premain started");
        // 使用byte-buddy作为插桩，不同版本jdk不兼容，使用asm
//        new AgentBuilder.Default()
////                .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
////                .with(AgentBuilder.InstallationListener.StreamWriting.toSystemOut())
//                .type(ElementMatchers.isSubTypeOf(ApplicationListener.class)
//                        .and(ElementMatchers.not(ElementMatchers.isInterface())))
//                .transform(new MyTransformer()).installOn(inst);
        inst.addTransformer(new SpringRefreshTransformer());
    }

    /**
     * 接收插件给的参数，调用方法
     *
     * @param args main方法的参数
     * @param inst 仪表
     */
    public static void agentmain(String args, Instrumentation inst) {
        if (args.startsWith("file://")) {
            try {
                args = IOUtil.getTextFileAsString(new File(URLDecoder.decode(args.substring(7), "UTF-8")));
            } catch (IOException e) {
                System.out.println(ThrowUtil.printStackTrace(e));
                return;
            }
        }
        RequestInfo requestInfo;
        try {
            requestInfo = JsonUtil.toJavaBean(Base64Util.decode(args), RequestInfo.class);
        } catch (Exception e) {
            System.err.println("[Agent] " + ThrowUtil.printStackTrace(e));
            return;
        }
        System.out.println("[Agent] agentmain invoked with class: " + requestInfo.getClassName());
        System.out.println("[Agent] agentmain invoked with method: " + requestInfo.getMethodName());
        if (Objects.nonNull(requestInfo.getRequestJson()) && !requestInfo.getRequestJson().isEmpty()) {
            System.out.println("[Agent] agentmain invoked with requestJson: " + requestInfo.getRequestJson());
        }
        if (Objects.nonNull(requestInfo.getParameterTypeList()) && !requestInfo.getParameterTypeList().isEmpty()) {
            System.out.println("[Agent] agentmain invoked with parameterTypeList: " + JsonUtil.toJsonString(requestInfo.getParameterTypeList()));
        }
        CompletableFuture.runAsync(() -> {
                    try {
                        AgentContextHolder.invoke(requestInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("[Agent] " + ThrowUtil.printStackTrace(throwable));
                    return null;
                });
    }
}
