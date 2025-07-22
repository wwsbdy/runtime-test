package com.zj.runtimetest.exp;

import com.zj.runtimetest.vo.ExpressionVo;
import com.zj.runtimetest.vo.RequestInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author : jie.zhou
 * @date : 2025/7/22
 */
public class RuntimeTestExprExecutor {
    private static final Map<String, ExpressionExecutor> cache = new ConcurrentHashMap<>();

    public interface ExpressionExecutor {
        Object[] eval(Object[] args);
    }

    public static synchronized ExpressionExecutor getExecutor(RequestInfo requestInfo) {
        try {
            ExpressionVo expVo = requestInfo.getExpVo();
            String expr = expVo.getMyExpression();

            ExpressionExecutor compiled = cache.get(expr);
            if (compiled == null) {
                compiled = RuntimeTestExpCompiler.compile(requestInfo);
                cache.put(expr, compiled);
            }
            return compiled;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static void clear() {
        cache.clear();
    }

    public static void remove(String expr) {
        cache.remove(expr);
    }
}