package com.zj.runtimetest.exp;

/**
 * @author : jie.zhou
 * @date : 2025/7/22
 */
public class ExpressionExecutorFactory {
    public static final RuntimeTestExprExecutor.ExpressionExecutor ERROR = new RuntimeTestExprExecutor.ExpressionExecutor() {
        @Override
        public Object[] eval(Object[] args) {
            System.err.println("[Agent] expression execution failed");
            return args;
        }
    };

    public static final RuntimeTestExprExecutor.ExpressionExecutor EMPTY = new RuntimeTestExprExecutor.ExpressionExecutor() {
        @Override
        public Object[] eval(Object[] args) {
            return args;
        }
    };

}
