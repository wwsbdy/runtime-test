package com.zj.runtimetest.exp;

/**
 * @author : jie.zhou
 * @date : 2025/7/22
 */
public class ExpressionExecutorFactory {
    public static final RuntimeTestExprExecutor.ExpressionExecutor ERROR = args -> {
        System.err.println("[Agent] expression execution failed");
        return args;
    };

    public static final RuntimeTestExprExecutor.ExpressionExecutor EMPTY = args -> args;

}
