package com.zj.runtimetest.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameterList;
import com.zj.runtimetest.cache.RuntimeTestState;
import com.zj.runtimetest.vo.CacheVo;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    public static @NotNull CacheVo getCacheOrDefault(PsiMethod psiMethod, Project project, String defaultJson) {
        CacheVo cache = getCache(psiMethod);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        cache = new CacheVo();
        PsiParameterList parameterList = psiMethod.getParameterList();
        cache.setClassName(((PsiClass) psiMethod.getParent()).getQualifiedName());
        cache.setMethodName(psiMethod.getName());
        cache.setParameterTypeList(ParamUtil.getParamTypeNameList(parameterList));
        cache.setProjectBasePath(project.getBasePath());
        cache.setStaticMethod(MethodUtil.isStaticMethod(psiMethod));
        cache.setRequestJson(defaultJson);
        return cache;
    }
}
