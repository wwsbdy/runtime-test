package com.zj.runtimetest.test;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.zj.runtimetest.test.cache.RuntimeTestState;
import org.jetbrains.annotations.NotNull;

/**
 * 进程监听
 * @author : lgp547
 * @date : 2025/6/13
 */
public class RuntimeTestExecutionListener implements ExecutionListener {

    @Override
    public void processStarting(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        Project project = env.getProject();
        try {
            if (handler instanceof KillableColoredProcessHandler.Silent) {
                long pid = ((KillableColoredProcessHandler.Silent) handler).getProcess().pid();
                RuntimeTestState.getInstance(project).putPidProcessMap(pid, env.toString());
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void processTerminated(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler, int exitCode) {
        Project project = env.getProject();
        try {
            if (handler instanceof KillableColoredProcessHandler.Silent) {
                long pid = ((KillableColoredProcessHandler.Silent) handler).getProcess().pid();
                RuntimeTestState.getInstance(project).removePidProcessMap(pid);
            }
        } catch (Exception ignored) {
        }
    }
}
