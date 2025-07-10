package com.zj.runtimetest.vo;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import lombok.Data;

/**
 * @author 19242
 */
@Data
public class ExpressionVo {
    private String myExpression;
    private String myLanguage;
    private String myCustomInfo;
    private String myMode;

    public XExpression toExpression() {
        return new XExpressionImpl(myExpression, Language.findLanguageByID(myLanguage), myCustomInfo, EvaluationMode.valueOf(myMode));
    }

    public static ExpressionVo fromExpression(XExpression expression) {
        ExpressionVo vo = new ExpressionVo();
        vo.myExpression = expression.getExpression();
        vo.myLanguage = expression.getLanguage() == null ? null : expression.getLanguage().getID();
        vo.myCustomInfo = expression.getCustomInfo();
        vo.myMode = expression.getMode().name();
        return vo;
    }

    public static class EmptyXExpression {
        public static final XExpression INSTANCE;
        static {
            INSTANCE = new XExpressionImpl("", JavaLanguage.INSTANCE, null, EvaluationMode.CODE_FRAGMENT);
//            INSTANCE = XExpressionImpl.fromText("", EvaluationMode.CODE_FRAGMENT);
        }
    }
}
