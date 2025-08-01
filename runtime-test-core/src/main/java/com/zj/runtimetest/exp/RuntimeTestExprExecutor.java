package com.zj.runtimetest.exp;

import com.zj.runtimetest.utils.LogUtil;
import com.zj.runtimetest.utils.ThrowUtil;
import com.zj.runtimetest.vo.ExpressionVo;
import com.zj.runtimetest.vo.MethodParamTypeInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author : jie.zhou
 * @date : 2025/7/22
 */
public class RuntimeTestExprExecutor {
    public static final Map<String, ExpressionExecutor> CACHE = new ConcurrentHashMap<>();

    public static ExpressionExecutor getExecutor(ExpressionVo expVo, List<MethodParamTypeInfo> parameterTypeList, String projectBasePath) {
        if (Objects.isNull(expVo)) {
            return ExpressionExecutorFactory.EMPTY;
        }
        ExpressionExecutor compiled;
        try {
            compiled = compileInMemory(expVo, parameterTypeList);
        } catch (Throwable t) {
            LogUtil.alwaysErr(ThrowUtil.printStackTrace(t));
            compiled = ExpressionExecutorFactory.ERROR;
        }
        return compiled;
    }

    public static Object[] evaluate(ExpressionVo expVo,
                                    List<MethodParamTypeInfo> parameterTypeList,
                                    String projectBasePath,
                                    Object[] args) {
        if (Objects.isNull(expVo)) {
            return args;
        }
        String key = getKey(expVo, parameterTypeList);
        LogUtil.log("[Agent more] pre-processing class cache key: " + key);
        ExpressionExecutor executor = CACHE.get(key);
        if (Objects.nonNull(executor)) {
            if (ExpressionExecutorFactory.ERROR == executor) {
                LogUtil.err("[Agent more] build pre-processing class is cached, error");
            } else if (ExpressionExecutorFactory.EMPTY == executor) {
                LogUtil.log("[Agent more] build pre-processing class is cached, empty");
            } else {
                LogUtil.log("[Agent more] build pre-processing class is cached, code: " + executor.getClassStr());
            }
        } else {
            executor = getExecutor(expVo, parameterTypeList, projectBasePath);
            CACHE.put(key, executor);
        }
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(RuntimeTestClassLoader.defaultClassLoader());
            return executor.eval(args);
        } catch (Throwable t) {
            CACHE.put(key, ExpressionExecutorFactory.ERROR);
            throw new RuntimeException(t);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private static String getKey(ExpressionVo expVo, List<MethodParamTypeInfo> methodParamTypeInfoList) {
        StringBuilder cacheKey = new StringBuilder();
        for (MethodParamTypeInfo methodParamTypeInfo : methodParamTypeInfoList) {
            cacheKey.append(methodParamTypeInfo.getParamName())
                    .append("|")
                    .append(methodParamTypeInfo.getType().getTypeName())
                    .append("|");
        }
        cacheKey.append(expVo.getMyExpression());
        cacheKey.append("|");
        cacheKey.append(expVo.getMyCustomInfo());
        return cacheKey.toString();
    }

    public static ExpressionExecutor compileInMemory(ExpressionVo expVo, List<MethodParamTypeInfo> parameterTypes) throws Exception {

        String expr = expVo.getMyExpression();
        if (expr == null || expr.isEmpty()) {
            return ExpressionExecutorFactory.EMPTY;
        }

        List<String> imports = Optional.ofNullable(expVo.getMyCustomInfo())
                .map(ip -> Stream.of(ip.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());


        String className = "ExprDynamic_" + Math.abs(expr.hashCode()) + "_" + System.currentTimeMillis();
        String packageName = "agent";
        String fullName = packageName + "." + className;
        // 构建 Java 源码
        String source = buildClassStr(parameterTypes, imports, className, expr).toString();
        LogUtil.log("[Agent more] build pre-processing class code: " + source);
        Class<?> clazz;
        try {
            clazz = PureECJCompiler.buildClass(fullName, source);
        } catch (Exception e) {
            LogUtil.err("[Agent more] build pre-processing class fail: " + ThrowUtil.printStackTrace(e));
            return ExpressionExecutorFactory.ERROR;
        }
        ExpressionExecutor expressionExecutor = (ExpressionExecutor) clazz.getDeclaredConstructor().newInstance();
        expressionExecutor.setClassStr(source);
        return expressionExecutor;
    }

    private static StringBuilder buildClassStr(List<MethodParamTypeInfo> parameterTypes, List<String> imports, String className, String expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("package agent;\n");
        for (String imp : imports) {
            sb.append("import ").append(imp).append(";\n");
        }
        sb.append("import com.zj.runtimetest.exp.ExpressionExecutor;\n\n");
        sb.append("public class ").append(className)
                .append(" extends ExpressionExecutor {\n")
                .append("    public Object[] eval(Object[] args) {\n")
                .append("        printBegin();\n");

        for (int i = 0; i < parameterTypes.size(); i++) {
            MethodParamTypeInfo methodParamTypeInfo = parameterTypes.get(i);
            // 把内部类的 $ 替换成 .
            String typeName = methodParamTypeInfo.getType().getTypeName().replaceAll("(?<!\\$)\\$(?!\\$)", ".");
            String paramName = methodParamTypeInfo.getParamName();
            sb.append("        ").append(typeName).append(" ").append(paramName)
                    .append(" = (").append(typeName).append(") args[").append(i).append("];\n");
        }
        sb.append("        try {\n").append(expr)
                .append(";\n        } catch (Throwable t) { throw new RuntimeException(t); }\n")
                .append("        printEnd();\n");

        if (parameterTypes.isEmpty()) {
            sb.append("        return null;\n");
        } else {
            sb.append("        return new Object[]{");
            for (int i = 0; i < parameterTypes.size(); i++) {
                sb.append(parameterTypes.get(i).getParamName());
                if (i < parameterTypes.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("};\n");
        }
        sb.append("    }\n");
        sb.append("}\n");
        return sb;
    }

}