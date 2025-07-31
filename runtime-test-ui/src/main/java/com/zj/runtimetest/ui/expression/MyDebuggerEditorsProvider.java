package com.zj.runtimetest.ui.expression;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.xdebugger.XExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.JavaDebuggerEditorsProvider;

/**
 * @author : jie.zhou
 * @date : 2025/7/31
 */
public class MyDebuggerEditorsProvider extends JavaDebuggerEditorsProvider {

    @Override
    protected PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull String text, @Nullable PsiElement context, boolean isPhysical) {
        PsiFile expressionCodeFragment = super.createExpressionCodeFragment(project, text, context, isPhysical);
        setChecker(expressionCodeFragment);
        return expressionCodeFragment;
    }

    private void setChecker(PsiFile expressionCodeFragment) {
        if (expressionCodeFragment instanceof JavaCodeFragment) {
            ((JavaCodeFragment) expressionCodeFragment).setVisibilityChecker((psiElement, psiElement1) -> {
                if (psiElement instanceof PsiMethod method1) {
                    if (method1.hasModifierProperty(PsiModifier.PRIVATE)) {
                        return JavaCodeFragment.VisibilityChecker.Visibility.NOT_VISIBLE;
                    }
                }
                if (psiElement instanceof PsiField field) {
                    if (field.hasModifierProperty(PsiModifier.PRIVATE)) {
                        return JavaCodeFragment.VisibilityChecker.Visibility.NOT_VISIBLE;
                    }
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
