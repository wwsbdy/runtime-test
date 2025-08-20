package com.zj.runtimetest.utils;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;

/**
 * 方法工具类
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

    public static boolean isConstructor(PsiMethod method) {
        if (method == null) {
            return false;
        }
        return method.isConstructor();
    }
}
