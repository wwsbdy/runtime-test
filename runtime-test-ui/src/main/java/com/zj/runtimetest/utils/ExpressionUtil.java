package com.zj.runtimetest.utils;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.zj.runtimetest.vo.ExpressionVo;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author : jie.zhou
 * @date : 2025/8/5
 */
public class ExpressionUtil {

    public static class EmptyXExpression {
        public static final XExpression INSTANCE;

        static {
            INSTANCE = new XExpressionImpl("", JavaLanguage.INSTANCE, null, EvaluationMode.CODE_FRAGMENT);
//            INSTANCE = XExpressionImpl.fromText("", EvaluationMode.CODE_FRAGMENT);
        }
    }


    public static XExpression toExpression(ExpressionVo expVo) {
        if (Objects.isNull(expVo)) {
            return EmptyXExpression.INSTANCE;
        }
        return new XExpressionImpl(expVo.getMyExpression(), JavaLanguage.INSTANCE, expVo.getMyCustomInfo(), EvaluationMode.CODE_FRAGMENT);
    }

    public static ExpressionVo fromExpression(XExpression expression) {
        if (StringUtils.isBlank(expression.getExpression())) {
            return null;
        }
        ExpressionVo vo = new ExpressionVo();
        vo.setMyExpression(expression.getExpression().trim());
        vo.setMyCustomInfo(expression.getCustomInfo());
        return vo;
    }
}
