package com.zj.runtimetest.ui;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ui.JBDimension;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
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
public class PreMethodExpressionDialog extends DialogWrapper {

    private static final Logger log = Logger.getInstance(PreMethodExpressionDialog.class);

    private final Project project;

    private boolean disposed = false;

    private XDebuggerEditorBase expressionEditor;

    private CacheVo cache;
    private PsiMethod psiMethod;

    public PreMethodExpressionDialog(Project project,
                                     @NotNull CacheVo cache,
                                     PsiMethod psiMethod) {
        super(true);
        // 是否允许拖拽的方式扩大或缩小
        setResizable(true);
        // 设置会话框标题
        setTitle(PluginBundle.get("dialog.preMethodFunction.title"));
        // 获取到当前项目的名称
        this.project = project;
        this.cache = cache;
        this.psiMethod = psiMethod;
        // 触发一下init方法，否则swing样式将无法展示在会话框
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        XExpression xExpression = cache.getExpression();
        // 创建临时方法代码
        String classText = generateFakeClassText(psiMethod);
        PsiFile psiFile = PsiFileFactory.getInstance(project)
                .createFileFromText("TempClass.java", JavaFileType.INSTANCE, classText);
        PsiMethod method = PsiTreeUtil.findChildOfType(psiFile, PsiMethod.class);


        PsiElement contextElement = PsiTreeUtil.findChildOfType(method, PsiComment.class);
        if (Objects.isNull(contextElement)) {
            return null;
        }
        int offset = contextElement.getTextOffset();
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            virtualFile = new LightVirtualFile("TempClass.java", JavaLanguage.INSTANCE, classText);
        }
        // 4. 创建 SourcePosition（控制作用域）
        XSourcePosition position = XSourcePositionImpl.createByOffset(virtualFile, offset);
        // 5. 构建 XDebuggerExpressionEditor
        expressionEditor = new XDebuggerExpressionEditor(project, new JavaDebuggerEditorsProvider(), null, position, xExpression, true, true, false);
        JComponent component = expressionEditor.getComponent();
        component.setPreferredSize(new JBDimension(700, 300));
        return component;
    }
    public static String generateFakeClassText(PsiMethod psiMethod) {
        StringBuilder builder = new StringBuilder();
        builder.append("public class TempClass {\n");
        builder.append("    @Deprecated\n");
        builder.append("    private void fakeMethod(");

        PsiParameterList parameterList = psiMethod.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            PsiParameter param = parameters[i];
            PsiType type = param.getType();
            // 获取完整类型（包含包名）
            String typeText = type.getCanonicalText();
            builder.append(typeText).append(" ").append(param.getName());
            if (i < parameters.length - 1) {
                builder.append(", ");
            }
        }

        builder.append(") {\n");
        builder.append("        // cursor\n");
        builder.append("    }\n");
        // printPreProcessingMethod
        builder.append("    private void printPreProcessingMethod() {}\n");
        builder.append("}\n");

        return builder.toString();
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

