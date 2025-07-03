package com.zj.runtimetest.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.zj.runtimetest.cache.RuntimeTestState;
import com.zj.runtimetest.vo.CacheVo;
import com.zj.runtimetest.utils.CacheUtil;

/**
 * @author jie.zhou
 */
public class PluginCacheUtil extends CacheUtil {

    public static String genCacheKey(PsiClass psiClass, PsiMethod psiMethod) {
        return genCacheKey(
                psiClass.getQualifiedName(),
                psiMethod.getName(),
                ParamUtil.getParamGenericsTypeNameList(psiMethod.getParameterList())
        );
    }

    public static String genCacheKey(PsiMethod psiMethod) {
        return genCacheKey((PsiClass) psiMethod.getParent(), psiMethod);
    }

    public static CacheVo getCache(PsiMethod psiMethod) {
        String cacheKey = genCacheKey(psiMethod);
        return RuntimeTestState.getInstance(psiMethod.getProject()).getCache(cacheKey);
    }
}
