package com.zj.runtimetest.utils;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiParameterList;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.zj.runtimetest.vo.ExpressionVo;
import com.zj.runtimetest.vo.MethodParamInfo;
import com.zj.runtimetest.vo.ParamVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * @author : jie.zhou
 * @date : 2025/8/5
 */
public class ExpressionUtil {

    public static @NotNull ExpressionVo getDefaultExpression(PsiParameterList parameterList) {
        if (Objects.isNull(parameterList) || parameterList.getParametersCount() == 0) {
            return new ExpressionVo();
        }
        // TODO 没有兼容范型、[]、...
        List<MethodParamInfo> paramGenericsTypeNameList = ParamUtil.getParamGenericsTypeNameList(parameterList);
        if (CollectionUtils.isEmpty(paramGenericsTypeNameList)) {
            return new ExpressionVo();
        }
        StringBuilder expression = new StringBuilder();
        StringBuilder imports = new StringBuilder();
        for (MethodParamInfo methodParamInfo : paramGenericsTypeNameList) {
            String paramType = methodParamInfo.getParamType();
            String paramName = methodParamInfo.getParamName();
            ParamVo paramVo = ParamUtil.getParamVo(paramType);
            String className = paramVo.getClassName();
            String importName = paramVo.getImportName();
            if (StringUtils.isNotBlank(className)) {
                expression.append(className).append(" ").append(paramName).append(" = ").append(FiledUtil.getFieldNullValue(paramType)).append(";\n");
            }
            if (StringUtils.isNotBlank(importName)) {
                imports.append(importName).append(",");
            }
        }
        return new ExpressionVo(expression.toString(), imports.toString());
    }

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
