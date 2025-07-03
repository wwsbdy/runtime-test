package com.zj.runtimetest.test.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.zj.runtimetest.test.cache.RuntimeTestState;
import com.zj.runtimetest.test.vo.CacheVo;
import com.zj.runtimetest.vo.MethodParamInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jie.zhou
 */
public class CacheUtil {

    public static String genCacheKey(PsiClass psiClass, PsiMethod psiMethod) {
        return genCacheKey(
                psiClass.getQualifiedName(),
                psiMethod.getName(),
                ParamUtil.getParamGenericsTypeNameList(psiMethod.getParameterList()).stream()
                        .map(MethodParamInfo::getParamType)
                        .collect(Collectors.toList())
        );
    }

    public static String genCacheKey(String className, String methodName, List<String> paramTypeNameList) {
        return className + "#" + methodName + "#" + String.join(",", paramTypeNameList);
    }

    public static String genCacheKey(PsiMethod psiMethod) {
        return genCacheKey((PsiClass) psiMethod.getParent(), psiMethod);
    }

    public static CacheVo getCache(PsiMethod psiMethod) {
        String cacheKey = genCacheKey(psiMethod);
        return RuntimeTestState.getInstance(psiMethod.getProject()).getCache(cacheKey);
    }
}
