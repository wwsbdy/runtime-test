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
    private static final Map<Object, Set<ClassLoader>> CONTEXT_CLASS_LOADER_MAP = new HashMap<>();
    private static final ObjCache<String, BeanInfo> BEAN_CACHE = new ObjCache<>(10);
    private static final ObjCache<String, MethodInvokeInfo> METHOD_CACHE = new ObjCache<>(10);
    private static final ClassLoader DEFAULT_CLASS_LOADER = Thread.currentThread().getContextClassLoader();

    public static void setContext(Object ctx) {
        System.out.println("[Agent] ApplicationContext injected." + ctx.getClass().getName());
        CONTEXT_CLASS_LOADER_MAP.put(ctx, new LinkedHashSet<>());
    }

    public static void invoke(RequestInfo requestInfo) {
        String className = requestInfo.getClassName();
        String methodName = requestInfo.getMethodName();
        ClassLoader classLoader;
        Object bean = null;
        if (requestInfo.isStaticMethod()) {
            System.out.println("[Agent] " + className + "." + methodName + "() is static.");
            classLoader = DEFAULT_CLASS_LOADER;
        } else {
            BeanInfo beanInfo = getBean(className);
            bean = beanInfo.getBean();
            if (Objects.isNull(bean)) {
                System.out.println("[Agent] Bean not found: " + className);
                return;
            }
            classLoader = beanInfo.getClassLoader();
            System.out.println("[Agent] Bean from: " + bean);
        }
        try {
            MethodInvokeInfo methodInvokeInfo = new MethodInvokeInfo(requestInfo, classLoader, bean);
            METHOD_CACHE.put(CacheUtil.genCacheKey(className, methodName, requestInfo.getParameterTypeList()), methodInvokeInfo);
            Object result = new MethodInvokeInfo(requestInfo, classLoader, bean).invoke();
            System.out.println("[Agent] " + methodName + "() invoked successfully. result: " + JsonUtil.toJsonString(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (CONTEXT_CLASS_LOADER_MAP.isEmpty()) {
            System.out.println("[Agent] init context classLoader map is empty.");
            NoSpringBeanInfo noSpringBeanInfo = new NoSpringBeanInfo(className, DEFAULT_CLASS_LOADER);
            BEAN_CACHE.put(className, noSpringBeanInfo);
            return noSpringBeanInfo;
        }
        if (!isInit) {
            try {
                initContextClassLoaderMap();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (Map.Entry<Object, Set<ClassLoader>> entry : CONTEXT_CLASS_LOADER_MAP.entrySet()) {
            for (ClassLoader classLoader : entry.getValue()) {
                Object context = entry.getKey();
                Object bean;
                try {
                    Class<?> clazz = ClassUtil.getClass(className, classLoader);
                    bean = context.getClass().getMethod("getBean", Class.class).invoke(context, clazz);
                } catch (Exception e) {
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
            if (CONTEXT_CLASS_LOADER_MAP.isEmpty()) {
                System.out.println("[Agent] init context classLoader map is empty.");
                return;
            }
            for (Map.Entry<Object, Set<ClassLoader>> entry : CONTEXT_CLASS_LOADER_MAP.entrySet()) {
                Object context = entry.getKey();
                Map<ClassLoader, Integer> classLoaderCountMap = new HashMap<>();
                Set<ClassLoader> classLoaders;
                for (String beanDefinitionName : (String[]) context.getClass().getMethod("getBeanDefinitionNames").invoke(context)) {
                    Object bean = context.getClass().getMethod("getBean", String.class).invoke(context, beanDefinitionName);
                    classLoaderCountMap.compute(bean.getClass().getClassLoader(), (k, v) -> v == null ? 1 : v + 1);
                }
                classLoaders = classLoaderCountMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .map(Map.Entry::getKey)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                entry.setValue(classLoaders);
                System.out.println("[Agent] " + entry.getKey() + " init classLoaders: " + classLoaders);
            }
        }
    }
}