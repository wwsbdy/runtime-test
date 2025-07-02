package com.zj.runtimetest.test.utils;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;

/**
 * @author : jie.zhou
 * @date : 2025/7/2
 */
public class MethodUtil {
    public static boolean isStaticMethod(PsiMethod method) {
        if (method == null) {
            return false;
        }
        PsiModifierList modifierList = method.getModifierList();
        return modifierList.hasModifierProperty(PsiModifier.STATIC);
    }

    public static boolean isPublicMethod(PsiMethod method) {
        if (method == null) {
            return false;
        }
        PsiModifierList modifierList = method.getModifierList();
        return modifierList.hasModifierProperty(PsiModifier.PUBLIC);
    }
}
