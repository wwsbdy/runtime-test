package com.zj.runtimetest.utils;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.zj.runtimetest.vo.ExpressionVo;
import com.zj.runtimetest.vo.ParamVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

/**
 * @author : jie.zhou
 * @date : 2025/8/5
 */
public class ExpressionUtil {

    public static @NotNull ExpressionVo getDefaultExpression(PsiMethod psiMethod) {
        PsiParameterList parameterList = psiMethod.getParameterList();
        StringBuilder expression = new StringBuilder();
        StringBuilder imports = new StringBuilder();
        if (parameterList.getParametersCount() == 0) {
            return new ExpressionVo(expression.toString(), imports.toString());
        }
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            PsiType type = parameter.getType();
            ParamVo paramVo = ParamUtil.getParamVo(type);
            String className = paramVo.getClassName();
            Set<String> importNames = paramVo.getImportNames();
            if (CollectionUtils.isNotEmpty(importNames)) {
                importNames.forEach(importName -> imports.append(importName).append(","));
            }
            String clsName = parameter.getType().getCanonicalText();
            if (StringUtils.isNotBlank(className)) {
                expression.append(className).append(" ").append(parameter.getName()).append(" = ").append(FiledUtil.getFieldNullValue(clsName)).append(";\n");
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
