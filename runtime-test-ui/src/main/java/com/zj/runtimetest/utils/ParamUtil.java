package com.zj.runtimetest.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.zj.runtimetest.utils.json.POJO2JSONParser;
import com.zj.runtimetest.vo.MethodParamInfo;
import com.zj.runtimetest.vo.ParamVo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : jie.zhou
 * @date : 2025/6/30
 */
public class ParamUtil {

    /**
     * 获取参数类型列表，paramType为可直接Class.forName(paramType)的 兼容内部类$；不包含范型
     */
    public static List<MethodParamInfo> getParamGenericsTypeNameList(PsiParameterList parameterList) {
        if (parameterList == null || parameterList.getParametersCount() == 0) {
            return Collections.emptyList();
        }
        List<MethodParamInfo> parameterTypeList = new ArrayList<>();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            String canonicalText;
            String clsName = parameter.getType().getCanonicalText();
            if (!ClassUtil.isPrimitive(clsName) && !clsName.contains(".")) {
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
            objectNode.putPOJO(parameter.getName(), POJO2JSONParser.parseFieldValue(parameter.getType()));
        }
        return JsonUtil.toJsonString(objectNode);
    }

    /**
     * 返回要添加到 import 区块的 FQN 集合（已去重、排序）。
     */
    public static Set<String> collectImportsForMethod(PsiMethod method) {
        Set<String> result = new TreeSet<>();
        if (method == null) {
            return result;
        }
        result.addAll(collectImportsFromType(method.getReturnType()));
        for (PsiParameter param : method.getParameterList().getParameters()) {
            result.addAll(collectImportsFromType(param.getType()));
        }
        return result;
    }

    private static Set<String> collectImportsFromType(PsiType type) {
        if (type == null) {
            return Collections.emptySet();
        }
        // 基本类型直接忽略
        if (type instanceof PsiPrimitiveType) {
            return Collections.emptySet();
        }

        // 数组 / varargs
        if (type instanceof PsiArrayType) {
            return collectImportsFromType(((PsiArrayType) type).getComponentType());
        }
        Set<String> out = new HashSet<>();
        // 泛型/类类型
        if (type instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) type).resolve();
            if (psiClass != null) {
                // <--- 返回外层顶级类的 FQN
                String topLevelFqn = getTopLevelQualifiedName(psiClass);
                if (topLevelFqn != null && !topLevelFqn.startsWith("java.lang.")) {
                    out.add(topLevelFqn);
                }
            } else {
                // 未解析的类型（例如源码未解析或依赖缺失）使用 canonicalText 作为回退，但同样过滤 java.lang
                String txt = type.getCanonicalText();
                if (!txt.startsWith("java.lang.")) {
                    out.add(txt);
                }
            }
            // 递归处理泛型参数
            for (PsiType param : ((PsiClassType) type).getParameters()) {
                out.addAll(collectImportsFromType(param));
            }
            return out;
        }

        // 通配符 ? extends / super
        if (type instanceof PsiWildcardType) {
            PsiType bound = ((PsiWildcardType) type).getBound();
            out.addAll(collectImportsFromType(bound));
            return out;
        }

        // 交集类型 A & B
        if (type instanceof PsiIntersectionType) {
            for (PsiType t : ((PsiIntersectionType) type).getConjuncts()) {
                out.addAll(collectImportsFromType(t));
            }
            return out;
        }

        // 并集类型 (Java 7 multi-catch 形式)
        if (type instanceof PsiDisjunctionType) {
            for (PsiType t : ((PsiDisjunctionType) type).getDisjunctions()) {
                out.addAll(collectImportsFromType(t));
            }
            return out;
        }

        // 兜底：把 canonicalText 放进集合（但仍过滤 java.lang）
        String canon = type.getCanonicalText();
        if (!canon.startsWith("java.lang.")) {
            out.add(canon);
        }
        return out;
    }

    /**
     * 把一个 PsiClass 拉到最外层的 top-level class，并返回其 FQN（可能为 null，表示匿名或局部类）。
     */
    private static String getTopLevelQualifiedName(PsiClass psiClass) {
        PsiClass top = psiClass;
        while (top.getContainingClass() != null) {
            top = top.getContainingClass();
        }
        // 若为匿名/局部类则返回 null
        return top.getQualifiedName();
    }

    public static ParamVo getParamVo(PsiClass psiClass) {
        Set<String> importNames = null;
        String topLevelFqn = getTopLevelQualifiedName(psiClass);
        if (topLevelFqn != null && !topLevelFqn.startsWith("java.lang.")) {
            importNames = Collections.singleton(topLevelFqn);
        }
        return new ParamVo(getTypeName(psiClass), JsonUtil.convertName(psiClass.getName()), importNames);
    }

    public static ParamVo getParamVo(PsiType psiType) {
        return new ParamVo(getTypeName(psiType), JsonUtil.convertName(StringUtils.substringBefore(psiType.getPresentableText(), "<")), collectImportsFromType(psiType));
    }

    /**
     * 保留泛型、内部类、数组、varargs等的类型文本
     */
    public static String getTypeName(PsiType type) {
        String clsName = type.getCanonicalText();
        if (!ClassUtil.isPrimitive(clsName) && !clsName.contains(".")) {
            return "Object";
        }
        if (type instanceof PsiArrayType) {
            return getTypeName(((PsiArrayType) type).getComponentType()) + "[]";
        }
        if (type instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) type).resolve();
            if (psiClass != null) {
                String className = getTypeName(psiClass);
                // 拼泛型参数
                PsiType[] parameters = ((PsiClassType) type).getParameters();
                if (parameters.length > 0) {
                    String params = Arrays.stream(parameters)
                            .map(ParamUtil::getTypeName)
                            .collect(Collectors.joining(", "));
                    return className + "<" + params + ">";
                }
                return className;
            }
        }
        // 基本类型等
        return clsName;
    }

    /**
     * 生成 Outer.Inner 这种形式（不带包名）
     */
    public static String getTypeName(PsiClass psiClass) {
        String className = psiClass.getQualifiedName();
        if (StringUtils.isNotBlank(className) && !ClassUtil.isPrimitive(className) && !className.contains(".")) {
            return "Object";
        }
        Deque<String> names = new LinkedList<>();
        PsiClass current = psiClass;
        while (current != null) {
            names.addFirst(current.getName());
            current = current.getContainingClass();
        }
        return String.join(".", names);
    }
}
