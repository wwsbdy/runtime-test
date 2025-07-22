package com.zj.runtimetest.exp;

import com.zj.runtimetest.vo.ExpressionVo;
import com.zj.runtimetest.vo.MethodParamTypeInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author : jie.zhou
 * @date : 2025/7/22
 */
public class RuntimeTestExprExecutor {
    public static final Map<String, ExpressionExecutor> CACHE = new ConcurrentHashMap<>();

    public interface ExpressionExecutor {
        Object[] eval(Object[] args);
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
            compiled = RuntimeTestExpCompiler.compileInMemory(expVo, parameterTypeList);
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
                                              Object[] args) {
        if (Objects.isNull(expVo)) {
            return args;
        }
        ExpressionExecutor executor = getExecutor(expVo, parameterTypeList, projectBasePath);
        try {
            return executor.eval(args);
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
}