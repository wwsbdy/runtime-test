package com.zj.runtimetest.listener;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.project.Project;
import com.zj.runtimetest.cache.RuntimeTestState;
import com.zj.runtimetest.vo.ProcessVo;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 进程监听
 * @author : lgp547
 * @date : 2025/6/13
 */
public class RuntimeTestExecutionListener implements ExecutionListener {

    @Override
    public void processStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        // TODO 是否可以在启动完成后再放入pid，防止启动未完成就点运行
        Project project = env.getProject();
        try {
            if (handler instanceof KillableColoredProcessHandler.Silent) {
                long pid = ((KillableColoredProcessHandler.Silent) handler).getProcess().pid();
                Long executionId = Optional.ofNullable(RunContentManager.getInstance(project).getSelectedContent())
                        .map(RunContentDescriptor::getExecutionId)
                        .orElse(null);
                RuntimeTestState.getInstance(project).putPidProcessMap(pid, new ProcessVo(pid, env.toString(), executionId, executorId));
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
