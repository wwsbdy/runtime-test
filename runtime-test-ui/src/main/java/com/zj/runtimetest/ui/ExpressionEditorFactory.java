package com.zj.runtimetest.ui;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.intellij.xdebugger.impl.ui.XDebuggerExpressionEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.JavaDebuggerEditorsProvider;

import java.util.Objects;

/**
 * @author : jie.zhou
 * @date : 2025/7/31
 */
public class ExpressionEditorFactory {

    public static @Nullable XDebuggerExpressionEditor createExpressionEditor(@NotNull Project project,
                                                                             @Nullable PsiMethod psiMethod,
                                                                             @NotNull XExpression xExpression) {
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
        // 创建 SourcePosition（控制作用域）
        XSourcePosition position = XSourcePositionImpl.createByOffset(virtualFile, offset);
        // 构建 XDebuggerExpressionEditor
        return new XDebuggerExpressionEditor(project, new JavaDebuggerEditorsProvider(), null, position, xExpression, true, true, false);
    }

    public static String generateFakeClassText(PsiMethod psiMethod) {
        StringBuilder builder = new StringBuilder();
        builder.append("public class TempClass {\n");
        builder.append("    @Deprecated\n");
        builder.append("    private void fakeMethod(");

        if (Objects.nonNull(psiMethod)) {
            PsiParameterList parameterList = psiMethod.getParameterList();
            PsiParameter[] parameters = parameterList.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                PsiParameter param = parameters[i];
                PsiType type = param.getType();
                // 获取完整类型（包含包名）
                String typeText = type.getCanonicalText();
                if (!typeText.contains(".")) {
                    typeText = "java.lang.Object";
                }
                builder.append(typeText).append(" ").append(param.getName());
                if (i < parameters.length - 1) {
                    builder.append(", ");
                }
            }
        }

        builder.append(") {\n");
        builder.append("        // cursor\n");
        builder.append("    }\n");
        // printPreProcessingMethod
        builder.append("    private void printPreProcessingMethod() {}\n");
        // addHeader
        builder.append("    private void addHeader(String name, Object value) {}\n");
        // setAttribute
        builder.append("    private void setAttribute(String name, Object value) {}\n");
        // getBean
        builder.append("    private <T> T getBean(Class<T> clz)) {}\n");
        builder.append("}\n");

        return builder.toString();
    }
}
