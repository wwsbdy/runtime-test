package com.zj.runtimetest.debug.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBDimension;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.intellij.xdebugger.impl.ui.XDebuggerEditorBase;
import com.intellij.xdebugger.impl.ui.XDebuggerExpressionEditor;
import com.zj.runtimetest.debug.RuntimeTestBreakpointProperties;
import com.zj.runtimetest.language.PluginBundle;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

/**
 * @author arthur_zhou
 */
@Setter
@Getter
public class PreMethodExpressionDialog extends DialogWrapper {

    private static final Logger log = Logger.getInstance(PreMethodExpressionDialog.class);

    private final Project project;

    private boolean disposed = false;

    private XDebuggerEditorBase expressionEditor;

    private final @NotNull XLineBreakpoint<RuntimeTestBreakpointProperties> bp;

    public PreMethodExpressionDialog(Project project,@NotNull XLineBreakpoint<RuntimeTestBreakpointProperties> bp) {
        super(true);
        // 是否允许拖拽的方式扩大或缩小
        setResizable(true);
        // 设置会话框标题
        setTitle(PluginBundle.get("dialog.preMethodFunction.title"));
        // 获取到当前项目的名称
        this.project = project;
        this.bp = bp;
        // 触发一下init方法，否则swing样式将无法展示在会话框
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        XDebuggerEditorsProvider debuggerEditorsProvider = bp.getType().getEditorsProvider(bp, project);
        if (Objects.nonNull(debuggerEditorsProvider)) {
            XExpression xExpression = Objects.isNull(bp.getConditionExpression()) ? XExpressionImpl.fromText("") : bp.getConditionExpression();
            expressionEditor = new XDebuggerExpressionEditor(project, debuggerEditorsProvider, "runtimeTestLogExpression", bp.getSourcePosition(), xExpression, true, true, false);
//            expressionEditor = new XDebuggerExpressionComboBox(project, debuggerEditorsProvider, "runtimeTestLogExpression", bp.getSourcePosition(), true, true);
            expressionEditor.setExpression(xExpression);
            JComponent component = expressionEditor.getComponent();
            component.setPreferredSize(new JBDimension(450, 300));
            return component;
        }
        return null;
    }

    @Override
    protected void doOKAction() {
        if (Objects.nonNull(expressionEditor) && StringUtils.isNotEmpty(expressionEditor.getExpression().getExpression())) {
            bp.setConditionExpression(expressionEditor.getExpression());
        } else {
            XDebuggerManager.getInstance(project).getBreakpointManager().removeBreakpoint(bp);
        }
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        if (StringUtils.isEmpty(expressionEditor.getExpression().getExpression())) {
            XDebuggerManager.getInstance(project).getBreakpointManager().removeBreakpoint(bp);
        }
        super.doCancelAction();
    }

    @Override
    protected void dispose() {
        if (!disposed) {
            disposed = true;
            expressionEditor = null;
        }
        super.dispose();
    }
}

