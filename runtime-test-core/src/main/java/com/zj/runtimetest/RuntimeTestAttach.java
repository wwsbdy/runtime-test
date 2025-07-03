package com.zj.runtimetest;

import com.zj.runtimetest.utils.JsonUtil;
import com.zj.runtimetest.vo.RequestInfo;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.CompletableFuture;

/**
 * @author : jie.zhou
 * @date : 2025/6/25
 */
public class RuntimeTestAttach {

    /**
     * premain方法在JVM启动时执行，在main方法之前执行
     * 监听ApplicationContext，拦截ApplicationContext#refresh()方法，获取到spring上下文
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
     * @param args main方法的参数
     * @param inst 仪表
     */
    public static void agentmain(String args, Instrumentation inst) {
        System.out.println("[Agent] agentmain invoked with args: " + args);
        RequestInfo requestInfo = JsonUtil.toJavaBean(args, RequestInfo.class);
        CompletableFuture.runAsync(() -> AgentContextHolder.invoke(requestInfo))
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }
}
