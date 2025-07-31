package com.zj.runtimetest;

import com.zj.runtimetest.exp.RuntimeTestExprExecutor;
import com.zj.runtimetest.utils.CacheUtil;
import com.zj.runtimetest.utils.ClassUtil;
import com.zj.runtimetest.utils.JsonUtil;
import com.zj.runtimetest.utils.LogUtil;
import com.zj.runtimetest.vo.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 19242
 */
public class AgentContextHolder {


    private static boolean isInit = false;
    private static Set<Object> CONTEXT_CLASS_LOADER_SET = new HashSet<>();
    private static final ObjCache<ClassLoader, ObjCache<Object, Integer>> CLASS_LOADER_CONTEXT_MAP = new ObjCache<>();
    private static final ObjCache<String, BeanInfo> BEAN_CACHE = new ObjCache<>(10);
    private static final ObjCache<String, MethodInvokeInfo> METHOD_CACHE = new ObjCache<>(10);
    public static final ClassLoader DEFAULT_CLASS_LOADER = Thread.currentThread().getContextClassLoader();

    public static void setContext(Object ctx) {
        LogUtil.alwaysLog("[Agent] ApplicationContext injected." + ctx.getClass().getName());
        CONTEXT_CLASS_LOADER_SET.add(ctx);
    }

    public static void invoke(RequestInfo requestInfo) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String className = requestInfo.getClassName();
        String methodName = requestInfo.getMethodName();
        if (Objects.isNull(className) || className.isEmpty()
                || Objects.isNull(methodName) || methodName.isEmpty()) {
            RuntimeTestExprExecutor.evaluate(requestInfo.getExpVo(), Collections.emptyList(), requestInfo.getProjectBasePath(), null, null);
            return;
        }
        String cacheKey = CacheUtil.genCacheKey(className, methodName, requestInfo.getParameterTypeList());
        // 打印cacheKey
        LogUtil.log("[Agent more] cacheKey: " + cacheKey);
        MethodInvokeInfo methodInvokeInfo = METHOD_CACHE.get(cacheKey);
        if (Objects.isNull(methodInvokeInfo)) {
            if (requestInfo.isStaticMethod()) {
                LogUtil.log("[Agent more] " + className + "." + methodName + "() is static.");
                methodInvokeInfo = new MethodInvokeInfo(requestInfo, new BeanInfo(className, null, DEFAULT_CLASS_LOADER));
            } else {
                BeanInfo beanInfo = getBean(className);
                Object bean = beanInfo.getBean();
                if (Objects.isNull(bean)) {
                    LogUtil.alwaysErr("[Agent] Bean not found: " + className);
                    return;
                }
                methodInvokeInfo = new MethodInvokeInfo(requestInfo, beanInfo);
                LogUtil.log("[Agent more] Bean from: " + bean);
            }
            METHOD_CACHE.put(cacheKey, methodInvokeInfo);
        } else {
            LogUtil.log("[Agent more] " + className + "." + methodName + "() is cached.");
            if (requestInfo.isStaticMethod()) {
                LogUtil.log("[Agent more] " + className + "." + methodName + "() is a static method.");
            } else if (methodInvokeInfo.getBeanInfo() instanceof NoSpringBeanInfo) {
                LogUtil.log("[Agent more] " + className + "." + methodName + "() is not a method of spring bean.");
            } else if (Objects.nonNull(methodInvokeInfo.getBeanInfo().getBean())) {
                LogUtil.log("[Agent more] " + className + "." + methodName + "() is a method of spring bean.");
            }
        }
        LogUtil.log("[Agent more] " + className + "." + methodName + "() is invoked.");
        Object result = methodInvokeInfo.invoke(requestInfo.getExpVo(), requestInfo.getRequestJson());
        LogUtil.alwaysLog("[Agent] " + methodName + "() invoked successfully." + (methodInvokeInfo.isReturnValue() ? " result: " + JsonUtil.toJsonString(result) : ""));
    }

    public static BeanInfo getBean(String className) {
        if (Objects.isNull(className) || className.isEmpty()) {
            LogUtil.alwaysErr("[Agent] className is null.");
            return BeanInfo.empty();
        }
        BeanInfo cacheBeanInfo = BEAN_CACHE.get(className);
        if (Objects.nonNull(cacheBeanInfo)) {
            LogUtil.log("[Agent more] getBean from cache: " + className);
            if (cacheBeanInfo instanceof NoSpringBeanInfo) {
                LogUtil.log("[Agent more] " + className + " is not a spring bean.");
            } else if (Objects.nonNull(cacheBeanInfo.getBean())) {
                LogUtil.log("[Agent more] " + className + " is a spring bean.");
            }
            return cacheBeanInfo;
        }
        if (!isInit) {
            try {
                initContextClassLoaderMap();
            } catch (Exception e) {
                LogUtil.alwaysErr("[Agent] init context classLoader map failed: " + e.getMessage());
            }
        }
        if (CLASS_LOADER_CONTEXT_MAP.isEmpty()) {
            LogUtil.log("[Agent more] context classLoader map is empty. it will be created through a constructor");
            NoSpringBeanInfo noSpringBeanInfo = new NoSpringBeanInfo(className, DEFAULT_CLASS_LOADER);
            BEAN_CACHE.put(className, noSpringBeanInfo);
            return noSpringBeanInfo;
        }
        // accessOrder=true，表示最近访问的元素会排在最后，将能正常获取bean的类加载器和spring上下文放最后
        for (ClassLoader classLoader : CLASS_LOADER_CONTEXT_MAP.getKeys()) {
            ObjCache<Object, Integer> contextCache = CLASS_LOADER_CONTEXT_MAP.get(classLoader);
            Class<?> clazz;
            try {
                clazz = ClassUtil.getClass(className, classLoader);
            } catch (Exception e) {
                LogUtil.err("[Agent more] find class fail: " + className);
                continue;
            }
            if (Objects.isNull(contextCache) || contextCache.isEmpty()) {
                continue;
            }
            for (Object context : contextCache.getKeys()) {
                Object bean;
                try {
                    bean = context.getClass().getMethod("getBean", Class.class).invoke(context, clazz);
                } catch (Exception e) {
                    LogUtil.err("[Agent more] getBean fail: " + className + " from spring context: " + context + "; classLoader: " + classLoader);
                    continue;
                }
                if (Objects.nonNull(bean)) {
                    LogUtil.log("[Agent more] getBean from spring context: " + className + " from spring context: " + context + "; classLoader: " + classLoader);
                    BeanInfo beanInfo = new BeanInfo(className, bean, classLoader);
                    BEAN_CACHE.put(className, beanInfo);
                    return beanInfo;
                }
            }
        }
        // 如果spring中没有这个bean，new一个调用该方法
        LogUtil.log("[Agent more] not found Bean from context, it will be created through a constructor: " + className);
        NoSpringBeanInfo noSpringBeanInfo = new NoSpringBeanInfo(className, DEFAULT_CLASS_LOADER);
        BEAN_CACHE.put(className, noSpringBeanInfo);
        return noSpringBeanInfo;
    }

    private static void initContextClassLoaderMap() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        synchronized (AgentContextHolder.class) {
            if (isInit) {
                LogUtil.alwaysLog("[Agent] init context classLoader map is already.");
                return;
            }
            isInit = true;
            if (CONTEXT_CLASS_LOADER_SET.isEmpty()) {
                LogUtil.alwaysLog("[Agent] init context classLoader map is empty.");
                CONTEXT_CLASS_LOADER_SET = null;
                return;
            }
            for (Object context : CONTEXT_CLASS_LOADER_SET) {
                Map<ClassLoader, Integer> classLoaderCountMap = new HashMap<>();
                List<ClassLoader> classLoaders;
                for (String beanDefinitionName : (String[]) context.getClass().getMethod("getBeanDefinitionNames").invoke(context)) {
                    Object bean = context.getClass().getMethod("getBean", String.class).invoke(context, beanDefinitionName);
                    classLoaderCountMap.compute(bean.getClass().getClassLoader(), (k, v) -> v == null ? 1 : v + 1);
                }
                classLoaders = classLoaderCountMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!classLoaders.isEmpty()) {
                    for (ClassLoader classLoader : classLoaders) {
                        CLASS_LOADER_CONTEXT_MAP.computeIfAbsent(classLoader, k -> new ObjCache<>()).put(context, 1);
                    }
                }
                LogUtil.alwaysLog("[Agent] " + context + " init classLoaders: " + classLoaders);
            }
            CONTEXT_CLASS_LOADER_SET.clear();
            CONTEXT_CLASS_LOADER_SET = null;
        }
    }
}