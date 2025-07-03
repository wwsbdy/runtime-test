package com.zj.runtimetest.utils;

import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.openapi.ui.ComboBox;
import com.zj.runtimetest.json.JsonEditorField;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import java.awt.event.ActionListener;
import java.util.Objects;

/**
 * @author jie.zhou
 */
public class ExecutorUtil {

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

}