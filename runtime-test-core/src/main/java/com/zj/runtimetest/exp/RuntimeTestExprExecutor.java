package com.zj.runtimetest.exp;

import com.zj.runtimetest.utils.HttpServletRequestUtil;
import com.zj.runtimetest.vo.ExpressionVo;
import com.zj.runtimetest.vo.MethodParamTypeInfo;
import lombok.Data;

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

    @Data
    public abstract static class ExpressionExecutor {
        private String classStr;

        private Map<String, Object> headers;
        private Map<String, Object> attributes;

        public abstract Object[] eval(Object[] args);

        protected void fakeMethod(Object... args) {
            System.err.println("[Agent] Don not execute me");
        }

        protected void printPreProcessingMethod() {
            if (Objects.nonNull(classStr) && !classStr.isEmpty()) {
                System.out.println(classStr);
            }
        }

        protected void addHeader(String name, Object value) {
            if (Objects.isNull(headers)) {
                headers = new LinkedHashMap<>();
            }
            headers.put(name, value);
        }

        protected void setAttribute(String name, Object value) {
            if (Objects.isNull(attributes)) {
                attributes = new LinkedHashMap<>();
            }
            attributes.put(name, value);
        }
    }


    public static ExpressionExecutor getExecutor(ExpressionVo expVo, List<MethodParamTypeInfo> parameterTypeList, String projectBasePath) {
        if (Objects.isNull(expVo)) {
            return ExpressionExecutorFactory.EMPTY;
        }
        String expr = expVo.getMyExpression();
        ExpressionExecutor compiled = CACHE.get(expr);
        if (Objects.nonNull(compiled)) {
            return compiled;
        }
        try {
            compiled = compileInMemory(expVo, parameterTypeList);
        } catch (Throwable t) {
            t.printStackTrace();
            compiled = ExpressionExecutorFactory.ERROR;
        }
        CACHE.put(expr, compiled);
        return compiled;
    }

    public static Object[] evaluate(ExpressionVo expVo,
                                    List<MethodParamTypeInfo> parameterTypeList,
                                    String projectBasePath,
                                    Object[] args,
                                    Object httpServletRequest) {
        if (Objects.isNull(expVo)) {
            return args;
        }
        ExpressionExecutor executor = getExecutor(expVo, parameterTypeList, projectBasePath);
        try {
            Object[] resultArgs = executor.eval(args);
            if (HttpServletRequestUtil.hasHttpServletRequest()) {
                HttpServletRequestUtil.setRequestAttributes(httpServletRequest, executor.getAttributes(), executor.getHeaders());
            }
            return resultArgs;
        } catch (Throwable t) {
            put(expVo.getMyExpression(), ExpressionExecutorFactory.ERROR);
            throw new RuntimeException(t);
        }
    }

    public static void clear() {
        CACHE.clear();
    }

    public static void remove(String expr) {
        CACHE.remove(expr);
    }

    public static void put(String expr, ExpressionExecutor executor) {
        CACHE.put(expr, executor);
    }

    public static RuntimeTestExprExecutor.ExpressionExecutor compileInMemory(ExpressionVo expVo, List<MethodParamTypeInfo> parameterTypes) throws Exception {

        String expr = expVo.getMyExpression();
        if (expr == null || expr.isEmpty()) {
            return ExpressionExecutorFactory.EMPTY;
        }

        List<String> imports = Optional.ofNullable(expVo.getMyCustomInfo())
                .map(ip -> Stream.of(ip.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());


        String className = "ExprDynamic_" + Math.abs(expr.hashCode());
        String packageName = "agent";
        String fullName = packageName + "." + className;
        // 构建 Java 源码
        String source = buildClassStr(parameterTypes, imports, className, expr).toString();
        Class<?> clazz;
        try {
            clazz = PureECJCompiler.buildClass(fullName, source);
        } catch (Exception e) {
            return ExpressionExecutorFactory.ERROR;
        }
        RuntimeTestExprExecutor.ExpressionExecutor expressionExecutor = (RuntimeTestExprExecutor.ExpressionExecutor) clazz.getDeclaredConstructor().newInstance();
        expressionExecutor.setClassStr(source);
        return expressionExecutor;
    }

    private static StringBuilder buildClassStr(List<MethodParamTypeInfo> parameterTypes, List<String> imports, String className, String expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("package agent;\n");
        for (String imp : imports) {
            sb.append("import ").append(imp).append(";\n");
        }
        sb.append("import com.zj.runtimetest.exp.RuntimeTestExprExecutor.ExpressionExecutor;\n\n");
        sb.append("public class ").append(className)
                .append(" extends ExpressionExecutor {\n")
                .append("    public Object[] eval(Object[] args) {\n");

        for (int i = 0; i < parameterTypes.size(); i++) {
            MethodParamTypeInfo methodParamTypeInfo = parameterTypes.get(i);
            String typeName = methodParamTypeInfo.getType().getTypeName();
            String paramName = methodParamTypeInfo.getParamName();
            sb.append("        ").append(typeName).append(" ").append(paramName)
                    .append(" = (").append(typeName).append(") args[").append(i).append("];\n");
        }
        sb.append("        try {\n").append(expr)
                .append(";\n        } catch (Throwable t) { throw new RuntimeException(t); }\n")
                .append("        System.out.println(\"[Agent] pre-processing execution succeeded\");\n")
                .append("        return new Object[]{");
        for (int i = 0; i < parameterTypes.size(); i++) {
            sb.append(parameterTypes.get(i).getParamName());
            if (i < parameterTypes.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("};\n    }\n");
        sb.append("}\n");
        return sb;
    }

}