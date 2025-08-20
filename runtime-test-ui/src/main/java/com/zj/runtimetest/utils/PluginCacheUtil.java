package com.zj.runtimetest.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.zj.runtimetest.cache.RuntimeTestState;
import com.zj.runtimetest.constant.Constant;
import com.zj.runtimetest.vo.CacheAndKeyVo;
import com.zj.runtimetest.vo.CacheVo;
import com.zj.runtimetest.vo.ItemVo;
import com.zj.runtimetest.vo.MethodParamInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 缓存工具类
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
        } else if (StringUtils.isBlank(cache.getRequestJson())) {
            if (CollectionUtils.isNotEmpty(cache.getHistory())) {
                cache.setRequestJson(cache.getHistory().get(cache.getHistory().size() - 1));
            } else {
                cache.setRequestJson(defaultJson);
            }
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

    public static @Nullable ItemVo<String> getOneOfNotExistContentCacheKey(RuntimeTestState runtimeTestState) {
        for (int i = 0; i < Constant.TOOLWINDOW_CONTENT_CACHE_KEY_LIST.size(); i++) {
            String key = Constant.TOOLWINDOW_CONTENT_CACHE_KEY_LIST.get(i);
            CacheVo cache = runtimeTestState.getCache(key);
            if (Objects.isNull(cache)) {
                return new ItemVo<>(i, key);
            }
        }
        return null;
    }

    public static @NotNull List<ItemVo<CacheVo>> getAllExistContentCacheKey(RuntimeTestState runtimeTestState) {
        List<ItemVo<CacheVo>> cacheList = new ArrayList<>();
        for (int i = 0; i < Constant.TOOLWINDOW_CONTENT_CACHE_KEY_LIST.size(); i++) {
            String key = Constant.TOOLWINDOW_CONTENT_CACHE_KEY_LIST.get(i);
            CacheVo cache = runtimeTestState.getCache(key);
            if (Objects.nonNull(cache)) {
                cacheList.add(new ItemVo<>(i, cache));
            }
        }

        return cacheList;
    }
}
