package com.zj.runtimetest.exp;

import com.zj.runtimetest.utils.LogUtil;

/**
 * @author : jie.zhou
 * @date : 2025/7/22
 */
public class ExpressionExecutorFactory {
    public static final ExpressionExecutor ERROR = new ExpressionExecutor() {
        @Override
        public Object[] eval(Object[] args) {
            LogUtil.alwaysErr("[Agent] expression execution failed");
            return args;
        }
    };

    public static final ExpressionExecutor EMPTY = new ExpressionExecutor() {
        @Override
        public Object[] eval(Object[] args) {
            return args;
        }
    };

}
