package com.zj.runtimetest.utils;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import com.zj.runtimetest.language.PluginBundle;
import com.zj.runtimetest.vo.CacheVo;
import org.jetbrains.java.debugger.breakpoints.properties.JavaMethodBreakpointProperties;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author : jie.zhou
 * @date : 2025/7/11
 */
public class RunUtil {

    private static final Logger log = Logger.getInstance(RunUtil.class);

    public static void run(Project project, CacheVo cache, Function<Boolean, XLineBreakpoint<JavaMethodBreakpointProperties>> breakpointFunc) {
        try {
            // 延迟500ms，等待断点加载完成
            Thread.sleep(500);
        } catch (InterruptedException e) {
            BreakpointUtil.removeBreakpoint(project, breakpointFunc.apply(false));
            return;
        }
        String coreJarPath = PathManager.getPluginsPath() + File.separator + "runtime-test-ui" + File.separator + "lib" + File.separator + "runtime-test-core.jar";
        String pid = cache.getPid().toString();
        String requestJson = JsonUtil.toJsonString(cache);
        String jsonPath = project.getBasePath() + "/.idea/runtime-test/RequestInfo.json";
        if (requestJson.length() > 600 && PluginIOUtil.flushFile(jsonPath, requestJson)) {
            requestJson = "file://" + URLEncoder.encode(jsonPath, StandardCharsets.UTF_8);
        }
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(pid);
            vm.loadAgent(coreJarPath, requestJson);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Non-numeric value found")) {
                log.warn("jdk lower version attach higher version, can ignore");
            } else {
                BreakpointUtil.removeBreakpoint(project, breakpointFunc.apply(false));
                if (Objects.equals(e.getMessage(), "No such process")) {
                    NoticeUtil.error(project, "[RuntimeTest] " + PluginBundle.get("notice.error.no-such-process") + " " + pid);
                } else {
                    log.error("e: ", e);
                    NoticeUtil.error(project, "[RuntimeTest] " + e.getMessage());
                }
            }
        } catch (AgentLoadException e) {
            if ("0".equals(e.getMessage())) {
                log.warn("jdk higher version attach lower version, can ignore");
            } else {
                BreakpointUtil.removeBreakpoint(project, breakpointFunc.apply(false));
                log.error("AgentLoadException: ", e);
                NoticeUtil.error(project, "[RuntimeTest] " + e.getMessage());
            }
        } catch (AgentInitializationException e) {
            log.error("AgentInitializationException: ", e);
            NoticeUtil.error(project, "[RuntimeTest] " + e.getMessage());
        } catch (Exception e) {
            BreakpointUtil.removeBreakpoint(project, breakpointFunc.apply(false));
            log.error("Exception: ", e);
            NoticeUtil.error(project, "[RuntimeTest] " + e.getMessage());
        } finally {
            if (null != vm) {
                try {
                    vm.detach();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
