package com.zj.runtimetest.exp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author : jie.zhou
 * @date : 2025/7/22
 */
public class ExprExecutor {
    private static final Map<String, ExpressionExecutor> cache = new ConcurrentHashMap<>();

    public interface ExpressionExecutor {
        void eval(Object[] args);
    }

    public static synchronized void run(Object[] args, String expr, Map<String, String> varTypes, List<String> imports) {
        try {
            ExpressionExecutor compiled = cache.get(expr);
            if (compiled == null) {
                compiled = ExpressionCompiler.compile(expr, varTypes, imports);
                cache.put(expr, compiled);
            }
            compiled.eval(args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void clear() {
        cache.clear();
    }

    public static void remove(String expr) {
        cache.remove(expr);
    }
}