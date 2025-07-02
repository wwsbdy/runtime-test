package com.zj.runtimetest.test.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.zj.runtimetest.utils.JsonUtil;
import com.zj.runtimetest.vo.MethodParamInfo;
import com.zj.runtimetest.test.json.parser.POJO2JSONParser;
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


    public static String getDefaultJson(PsiParameterList parameterList) {
        ObjectNode objectNode = JsonUtil.objectMapper.createObjectNode();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            objectNode.putPOJO(JsonUtil.convertName(parameter.getName()), POJO2JSONParser.parseFieldValue(parameter.getType()));
        }
        return JsonUtil.toJsonString(objectNode);
    }

}
