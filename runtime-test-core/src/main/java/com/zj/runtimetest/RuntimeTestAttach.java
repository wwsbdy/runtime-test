package com.zj.runtimetest;

import com.zj.runtimetest.utils.*;
import com.zj.runtimetest.vo.RequestInfo;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URLDecoder;
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
        LogUtil.alwaysLog("[Agent] premain started");
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
                LogUtil.alwaysLog(ThrowUtil.printStackTrace(e));
                return;
            }
        }
        RequestInfo requestInfo;
        try {
            String requestInfoStr = Base64Util.decode(args);
            requestInfo = JsonUtil.toJavaBean(Base64Util.decode(args), RequestInfo.class);
            if (requestInfo.isDetailLog()) {
                LogUtil.alwaysLog("[Agent more] agentmain invoked with args: " + requestInfoStr);
            } else if (requestInfo.getRequestJson() != null && !requestInfo.getRequestJson().isEmpty()) {
                LogUtil.alwaysLog("[Agent] agentmain invoked with requestJson: " + requestInfo.getRequestJson());
            }
        } catch (Exception e) {
            LogUtil.alwaysErr("[Agent] " + ThrowUtil.printStackTrace(e));
            return;
        }
        CompletableFuture.runAsync(() -> {
                    try {
                        LogUtil.setDetailLog(requestInfo.isDetailLog());
                        // 此处初始化HttpServletRequest，防止被调用方法里有使用HttpServletRequest导致报错
                        HttpServletRequestUtil.getHttpServletRequest();
                        AgentContextHolder.invoke(requestInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        LogUtil.clear();
                        HttpServletRequestUtil.clear();
                    }
                })
                .exceptionally(throwable -> {
                    LogUtil.alwaysErr("[Agent] " + ThrowUtil.printStackTrace(throwable));
                    return null;
                });
    }
}
