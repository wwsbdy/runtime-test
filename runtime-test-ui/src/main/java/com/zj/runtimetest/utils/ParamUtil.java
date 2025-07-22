package com.zj.runtimetest.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.zj.runtimetest.vo.MethodParamInfo;
import com.zj.runtimetest.json.parser.POJO2JSONParser;
import org.apache.commons.lang3.StringUtils;

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
     *
     */
    public static List<MethodParamInfo> getParamTypeNameList(PsiParameterList parameterList) {
        if (parameterList == null) {
            return Collections.emptyList();
        }
        if (parameterList.getParametersCount() == 0) {
            return Collections.emptyList();
        }
        List<MethodParamInfo> parameterTypeList = new ArrayList<>();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            String canonicalText = parameter.getType().getCanonicalText();
            String classType = StringUtils.substringBefore(canonicalText, "<");
            parameterTypeList.add(new MethodParamInfo(parameter.getName(), classType));
        }
        return parameterTypeList;
    }

    /**
     * 获取参数类型列表（包含范型）
     *
     */
    public static List<MethodParamInfo> getParamGenericsTypeNameList(PsiParameterList parameterList) {
        if (parameterList == null || parameterList.getParametersCount() == 0) {
            return Collections.emptyList();
        }
        List<MethodParamInfo> parameterTypeList = new ArrayList<>();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            String canonicalText = parameter.getType().getCanonicalText();
            parameterTypeList.add(new MethodParamInfo(parameter.getName(), canonicalText));
        }
        return parameterTypeList;
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
