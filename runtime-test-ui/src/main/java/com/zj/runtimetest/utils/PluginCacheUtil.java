package com.zj.runtimetest.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.zj.runtimetest.cache.RuntimeTestState;
import com.zj.runtimetest.vo.CacheAndKeyVo;
import com.zj.runtimetest.vo.CacheVo;
import com.zj.runtimetest.vo.MethodParamInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jie.zhou
 */
public class PluginCacheUtil {
    public static @NotNull CacheAndKeyVo getCacheOrDefault(PsiMethod psiMethod, Project project, String defaultJson) {
        String className = ParamUtil.getJvmQualifiedClassName(((PsiClass) psiMethod.getParent()));
        String methodName = psiMethod.getName();
        List<MethodParamInfo> paramInfoList = ParamUtil.getParamGenericsTypeNameList(psiMethod.getParameterList());
        String cacheKey = CacheUtil.genCacheKey(className, methodName, paramInfoList);
        CacheVo cache = RuntimeTestState.getInstance(psiMethod.getProject()).getCache(cacheKey);
        if (Objects.isNull(cache)) {
            cache = new CacheVo();
            cache.setRequestJson(defaultJson);
        } else if (CollectionUtils.isNotEmpty(cache.getHistory())) {
            cache.setRequestJson(cache.getHistory().get(cache.getHistory().size() - 1));
        } else {
            cache.setRequestJson(defaultJson);
        }
        cache.setClassName(ParamUtil.getJvmQualifiedClassName(((PsiClass) psiMethod.getParent())));
        cache.setMethodName(psiMethod.getName());
        // 去掉范型
        cache.setParameterTypeList(
                paramInfoList.stream().peek(methodParamInfo -> methodParamInfo.setParamType(StringUtils.substringBefore(methodParamInfo.getParamType(), "<")))
                        .collect(Collectors.toList())
        );
        cache.setProjectBasePath(project.getBasePath());
        cache.setStaticMethod(MethodUtil.isStaticMethod(psiMethod));
        return new CacheAndKeyVo(cacheKey, cache);
    }
}
