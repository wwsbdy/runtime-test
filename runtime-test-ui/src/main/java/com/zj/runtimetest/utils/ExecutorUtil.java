package com.zj.runtimetest.utils;

import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.zj.runtimetest.cache.RuntimeTestState;
import com.zj.runtimetest.ui.json.JsonEditorField;
import com.zj.runtimetest.vo.ProcessVo;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import java.awt.event.ActionListener;
import java.util.Objects;

/**
 * @author jie.zhou
 */
public class ExecutorUtil {

    private static final Logger log = Logger.getInstance(ExecutorUtil.class);

    /**
     * 返回正在运行的 Executor
     *
     * @param id Executor id
     */
    public static Executor getRunExecutorInstance(String id) {
        return ExecutorRegistry.getInstance().getExecutorById(id);
    }


    public static void removeListener(AbstractButton button) {
        if (Objects.isNull(button)) {
            return;
        }
        ActionListener[] actionListeners = button.getActionListeners();
        if (Objects.isNull(actionListeners)) {
            return;
        }
        for (ActionListener actionListener : actionListeners) {
            button.removeActionListener(actionListener);
        }
    }

    public static void removeListener(ComboBox<?> rowComboBox) {
        if (Objects.isNull(rowComboBox)) {
            return;
        }
        ActionListener[] actionListeners = rowComboBox.getActionListeners();
        if (Objects.isNull(actionListeners)) {
            return;
        }
        for (ActionListener actionListener : actionListeners) {
            rowComboBox.removeActionListener(actionListener);
        }
    }

    public static void removeListener(JsonEditorField jsonEditorField) {
        if (Objects.isNull(jsonEditorField)) {
            return;
        }
        AncestorListener[] ancestorListeners = jsonEditorField.getAncestorListeners();
        if (Objects.isNull(ancestorListeners)) {
            return;
        }
        for (AncestorListener ancestorListener : ancestorListeners) {
            jsonEditorField.removeAncestorListener(ancestorListener);
        }
    }

    /**
     * 跳转指定的Run/Debug窗口
     *
     * @param pid 进程id
     */
    public static void toFrontRunContent(Project project, Long pid) {
        if (Objects.isNull(pid)) {
            log.info("toFrontRunContent pid is null");
            return;
        }
        ProcessVo process = RuntimeTestState.getInstance(project).getProcess(pid);
        if (Objects.isNull(process) || Objects.isNull(process.getExecutionId()) || StringUtils.isEmpty(process.getExecutorId())) {
            log.info("toFrontRunContent process is null");
            return;
        }
        Executor executor = ExecutorRegistry.getInstance().getExecutorById(process.getExecutorId());
        if (Objects.isNull(executor)) {
            log.info("toFrontRunContent executor is null");
            return;
        }
        RunContentManager runContentManager = RunContentManager.getInstance(project);
        RunContentDescriptor runContentDescriptor = runContentManager.getAllDescriptors().stream()
                .filter(descriptor -> process.getExecutionId().equals(descriptor.getExecutionId()))
                .findFirst()
                .orElse(null);
        if (Objects.isNull(runContentDescriptor)) {
            log.info("toFrontRunContent runContentDescriptor is null");
            return;
        }
        runContentManager.toFrontRunContent(executor, runContentDescriptor);
        ExecutionConsole console = runContentDescriptor.getExecutionConsole();
        if (console instanceof ConsoleView) {
            ConsoleView consoleView = (ConsoleView) console;
            consoleView.requestScrollingToEnd();
        }
    }

}