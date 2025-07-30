package com.zj.runtimetest.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.zj.runtimetest.json.parser.POJO2JSONParser;
import com.zj.runtimetest.vo.MethodParamInfo;
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
     * 获取 PsiType 对应的 JVM 格式类名（可直接用于 Class.forName）
     * 支持数组、多维数组、内部类（用 $ 分隔），可变参数视为数组
     *
     * @param type PsiType（参数类型）
     * @return JVM 格式的类名字符串
     */
    public static String getJvmQualifiedClassName(PsiType type) {
        int arrayDimensions = 0;
        // 展开数组类型
        while (type instanceof PsiArrayType) {
            arrayDimensions++;
            type = ((PsiArrayType) type).getComponentType();
        }
        // 解析基本类型（int, boolean 等）
        if (type instanceof PsiPrimitiveType) {
            String base = type.getCanonicalText();
            return base + "[]".repeat(arrayDimensions);
        }
        // 解析引用类型（类）
        PsiClass psiClass = PsiUtil.resolveClassInType(type);
        if (psiClass == null) {
            return type.getCanonicalText();
        }
        StringBuilder builder = new StringBuilder();
        buildJvmClassName(psiClass, builder);
        // 添加数组维度
        builder.append("[]".repeat(Math.max(0, arrayDimensions)));
        return builder.toString();
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
