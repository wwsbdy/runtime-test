package com.zj.runtimetest.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBDimension;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.intellij.xdebugger.impl.ui.XDebuggerEditorBase;
import com.intellij.xdebugger.impl.ui.XDebuggerExpressionEditor;
import com.zj.runtimetest.language.PluginBundle;
import com.zj.runtimetest.vo.CacheVo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.debugger.JavaDebuggerEditorsProvider;

import javax.swing.*;
import java.util.Objects;

/**
 * @author arthur_zhou
 */
@Setter
@Getter
public class PreMethodExpressionDialog<T extends XBreakpointProperties<?>> extends DialogWrapper {

    private static final Logger log = Logger.getInstance(PreMethodExpressionDialog.class);

    private final Project project;

    private boolean disposed = false;

    private XDebuggerEditorBase expressionEditor;

    private CacheVo cache;
    private VirtualFile file;
    private int line;

    public PreMethodExpressionDialog(Project project,
                                     @NotNull CacheVo cache,
                                     @NotNull VirtualFile file,
                                     int line) {
        super(true);
        // 是否允许拖拽的方式扩大或缩小
        setResizable(true);
        // 设置会话框标题
        setTitle(PluginBundle.get("dialog.preMethodFunction.title"));
        // 获取到当前项目的名称
        this.project = project;
        this.cache = cache;
        this.file = file;
        this.line = line;
        // 触发一下init方法，否则swing样式将无法展示在会话框
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        XExpression xExpression = cache.getExpression();
        XSourcePosition sourcePosition = XSourcePositionImpl.create(file, line + 1);
        expressionEditor = new XDebuggerExpressionEditor(project, new JavaDebuggerEditorsProvider(), "runtimeTestLogExpression", sourcePosition, xExpression, true, true, false);
//            expressionEditor = new XDebuggerExpressionComboBox(project, debuggerEditorsProvider, "runtimeTestLogExpression", bp.getSourcePosition(), true, true);
        expressionEditor.setExpression(xExpression);
        JComponent component = expressionEditor.getComponent();
        component.setPreferredSize(new JBDimension(700, 300));
        return component;
    }

    @Override
    protected void doOKAction() {
        XExpression expression;
        if (Objects.nonNull(expressionEditor)
                && Objects.nonNull(expression = expressionEditor.getExpression())
                && StringUtils.isNotBlank(expression.getExpression())) {
            cache.setExpression(expression);
        } else {
            cache.setExpression(CacheVo.EmptyXExpression.INSTANCE);
        }
        super.doOKAction();
    }

    @Override
    protected void dispose() {
        if (!disposed) {
            disposed = true;
            expressionEditor = null;
            cache = null;
            file = null;
        }
        super.dispose();
    }
}

