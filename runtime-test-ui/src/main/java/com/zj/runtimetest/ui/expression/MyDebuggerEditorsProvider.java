package com.zj.runtimetest.ui.expression;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.xdebugger.XExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.JavaDebuggerEditorsProvider;

/**
 * 自定义表达式编辑器
 * 禁用非公有方法、字段
 * @author : jie.zhou
 * @date : 2025/7/31
 */
public class MyDebuggerEditorsProvider extends JavaDebuggerEditorsProvider {

    public static final MyDebuggerEditorsProvider INSTANCE = new MyDebuggerEditorsProvider();

    @Override
    protected PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull String text, @Nullable PsiElement context, boolean isPhysical) {
        PsiFile expressionCodeFragment = super.createExpressionCodeFragment(project, text, context, isPhysical);
        setChecker(expressionCodeFragment);
        return expressionCodeFragment;
    }

    private void setChecker(PsiFile expressionCodeFragment) {
        if (expressionCodeFragment instanceof JavaCodeFragment) {
            ((JavaCodeFragment) expressionCodeFragment).setVisibilityChecker((psiElement, psiElement1) -> {
                if ((psiElement instanceof PsiMethod || psiElement instanceof PsiField || psiElement instanceof PsiClass)) {
                    if (((PsiModifierListOwner) psiElement).hasModifierProperty(PsiModifier.PUBLIC)) {
                        return JavaCodeFragment.VisibilityChecker.Visibility.VISIBLE;
                    }
                    return JavaCodeFragment.VisibilityChecker.Visibility.NOT_VISIBLE;
                }
                return JavaCodeFragment.VisibilityChecker.Visibility.VISIBLE;
            });
        }
    }

    @Override
    protected PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull XExpression expression, @Nullable PsiElement context, boolean isPhysical) {
        PsiFile expressionCodeFragment = super.createExpressionCodeFragment(project, expression, context, isPhysical);
        setChecker(expressionCodeFragment);
        return expressionCodeFragment;
    }
}
