package com.zj.runtimetest.ui.expression;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiMethod;
import com.intellij.util.ui.JBDimension;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.impl.ui.XDebuggerEditorBase;
import com.zj.runtimetest.language.PluginBundle;
import com.zj.runtimetest.vo.CacheVo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * @author arthur_zhou
 */
@Setter
@Getter
public class ExpressionDialog extends DialogWrapper {

    private static final Logger log = Logger.getInstance(ExpressionDialog.class);

    private final Project project;

    private boolean disposed = false;

    private XDebuggerEditorBase expressionEditor;

    private CacheVo cache;
    private @Nullable PsiMethod psiMethod;

    public ExpressionDialog(Project project,
                            @NotNull CacheVo cache,
                            @Nullable PsiMethod psiMethod) {
        super(true);
        // 是否允许拖拽的方式扩大或缩小
        setResizable(true);
        // 设置会话框标题
        setTitle(Objects.nonNull(psiMethod) ? PluginBundle.get("dialog.preMethodFunction.title") : PluginBundle.get("dialog.executionExpression.title"));
        // 获取到当前项目的名称
        this.project = project;
        this.cache = cache;
        this.psiMethod = psiMethod;
        // 触发一下init方法，否则swing样式将无法展示在会话框
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        expressionEditor = ExpressionEditorFactory.createExpressionEditor(project, psiMethod, cache.getExpression());
        if (Objects.isNull(expressionEditor)) {
            return null;
        }
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
            psiMethod = null;
        }
        super.dispose();
    }
}

