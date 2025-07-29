package com.zj.runtimetest.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;
import com.zj.runtimetest.json.parser.POJO2JSONParser;
import com.zj.runtimetest.vo.MethodParamInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author : jie.zhou
 * @date : 2025/6/30
 */
public class ParamUtil {

    /**
     * 获取参数类型列表（去掉范型）
     */
    public static List<MethodParamInfo> getParamTypeNameList(PsiParameterList parameterList) {
        List<MethodParamInfo> parameterTypeList = getParamGenericsTypeNameList(parameterList);
        for (MethodParamInfo methodParamInfo : parameterTypeList) {
            methodParamInfo.setParamType(StringUtils.substringBefore(methodParamInfo.getParamType(), "<"));
        }
        return parameterTypeList;
    }

    /**
     * 获取参数类型列表（包含范型）
     */
    public static List<MethodParamInfo> getParamGenericsTypeNameList(PsiParameterList parameterList) {
        if (parameterList == null || parameterList.getParametersCount() == 0) {
            return Collections.emptyList();
        }
        List<MethodParamInfo> parameterTypeList = new ArrayList<>();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            String canonicalText;
            if (!parameter.getType().getCanonicalText().contains(".")) {
                canonicalText = "java.lang.Object";
            } else {
                // 兼容内部类，用$
                canonicalText = getJvmQualifiedClassName(parameter.getType());
            }
            parameterTypeList.add(new MethodParamInfo(parameter.getName(), canonicalText));
        }
        return parameterTypeList;
    }

    /**
     * 获取 PsiType 对应的 JVM 类名（可用于 Class.forName），支持内部类用 $
     *
     * @param type PsiType 对象（例如来自 PsiParameter.getType()）
     * @return JVM 格式的类名，例如 com.example.Outer$Inner
     */
    public static String getJvmQualifiedClassName(PsiType type) {
        PsiClass psiClass = PsiUtil.resolveClassInType(type);
        if (psiClass == null) {
            // fallback，一般为原始文本
            return type.getCanonicalText();
        }
        return getJvmQualifiedClassName(psiClass);
    }

    /**
     * 获取 PsiType 对应的 JVM 类名（可用于 Class.forName），支持内部类用 $
     *
     * @param psiClass 对象
     * @return JVM 格式的类名，例如 com.example.Outer$Inner
     */
    public static String getJvmQualifiedClassName(@NotNull PsiClass psiClass) {
        StringBuilder nameBuilder = new StringBuilder();
        buildJvmClassName(psiClass, nameBuilder);
        return nameBuilder.toString();
    }

    // 递归构建 $ 分隔的 JVM 类型名
    private static void buildJvmClassName(PsiClass psiClass, StringBuilder builder) {
        PsiClass outerClass = psiClass.getContainingClass();
        if (outerClass != null) {
            buildJvmClassName(outerClass, builder);
            builder.append('$').append(psiClass.getName());
        } else {
            String qName = psiClass.getQualifiedName();
            if (qName != null) {
                builder.append(qName);
            }
        }
    }


    public static String getDefaultJson(PsiParameterList parameterList) {
        if (Objects.isNull(parameterList) || parameterList.getParametersCount() == 0) {
            return "";
        }
        ObjectNode objectNode = JsonUtil.objectMapper.createObjectNode();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            objectNode.putPOJO(JsonUtil.convertName(parameter.getName()), POJO2JSONParser.parseFieldValue(parameter.getType()));
        }
        return JsonUtil.toJsonString(objectNode);
    }

}
