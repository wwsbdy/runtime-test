package com.zj.runtimetest;

import com.zj.runtimetest.utils.CacheUtil;
import com.zj.runtimetest.utils.ClassUtil;
import com.zj.runtimetest.utils.JsonUtil;
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
        System.out.println("[Agent] ApplicationContext injected." + ctx.getClass().getName());
        CONTEXT_CLASS_LOADER_SET.add(ctx);
    }

    public static void invoke(RequestInfo requestInfo) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String className = requestInfo.getClassName();
        String methodName = requestInfo.getMethodName();
        String cacheKey = CacheUtil.genCacheKey(className, methodName, requestInfo.getParameterTypeList());
        MethodInvokeInfo methodInvokeInfo = METHOD_CACHE.get(cacheKey);
        if (Objects.isNull(methodInvokeInfo)) {
            ClassLoader classLoader;
            Object bean = null;
            if (requestInfo.isStaticMethod()) {
                System.out.println("[Agent] " + className + "." + methodName + "() is static.");
                classLoader = DEFAULT_CLASS_LOADER;
            } else {
                BeanInfo beanInfo = getBean(className);
                bean = beanInfo.getBean();
                if (Objects.isNull(bean)) {
                    System.err.println("[Agent] Bean not found: " + className);
                    return;
                }
                classLoader = beanInfo.getClassLoader();
                System.out.println("[Agent] Bean from: " + bean);
            }
            methodInvokeInfo = new MethodInvokeInfo(requestInfo, classLoader, bean);
            METHOD_CACHE.put(cacheKey, methodInvokeInfo);
        } else {
            System.out.println("[Agent] " + className + "." + methodName + "() is cached.");
        }
        Object result = methodInvokeInfo.invoke(requestInfo.getRequestJson());
        System.out.println("[Agent] success " + methodName + "() invoked successfully." + (methodInvokeInfo.isReturnValue() ? " result: " + JsonUtil.toJsonString(result) : ""));
    }

    public static BeanInfo getBean(String className) {
        if (Objects.isNull(className) || className.isEmpty()) {
            System.out.println("[Agent] className is null.");
            return BeanInfo.empty();
        }
        BeanInfo o = BEAN_CACHE.get(className);
        if (Objects.nonNull(o)) {
            System.out.println("[Agent] getBean from cache: " + className);
            return o;
        }
        if (!isInit) {
            try {
                initContextClassLoaderMap();
            } catch (Exception e) {
                System.out.println("[Agent] init context classLoader map failed: " + e.getMessage());
            }
        }
        if (CLASS_LOADER_CONTEXT_MAP.isEmpty()) {
            System.out.println("[Agent] context classLoader map is empty.");
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
//                System.err.println("[Agent] find class fail: " + className);
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
//                    System.err.println("[Agent] getBean fail: " + className);
                    continue;
                }
                if (Objects.nonNull(bean)) {
                    BeanInfo beanInfo = new BeanInfo(className, bean, classLoader);
                    BEAN_CACHE.put(className, beanInfo);
                    return beanInfo;
                }
            }
        }
        // 如果spring中没有这个bean，new一个调用该方法
        System.out.println("[Agent] not found Bean from context: " + className);
        NoSpringBeanInfo noSpringBeanInfo = new NoSpringBeanInfo(className, DEFAULT_CLASS_LOADER);
        BEAN_CACHE.put(className, noSpringBeanInfo);
        return noSpringBeanInfo;
    }

    private static void initContextClassLoaderMap() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        synchronized (AgentContextHolder.class) {
            if (isInit) {
                System.out.println("[Agent] init context classLoader map is already.");
                return;
            }
            isInit = true;
            if (CONTEXT_CLASS_LOADER_SET.isEmpty()) {
                System.out.println("[Agent] init context classLoader map is empty.");
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
                System.out.println("[Agent] " + context + " init classLoaders: " + classLoaders);
            }
            CONTEXT_CLASS_LOADER_SET.clear();
            CONTEXT_CLASS_LOADER_SET = null;
        }
    }
}