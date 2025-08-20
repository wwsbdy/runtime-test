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
 * 参数工具类
 * @author : jie.zhou
 * @date : 2025/6/30
 */
public class ParamUtil {
    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch",
            "char", "class", "const", "continue", "default", "do", "double",
            "else", "enum", "extends", "false", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "null", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "true", "try", "void", "volatile", "while", "var",
            "record"
    ));

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
            if (ClassUtil.isPrimitive(clsName)
                    || clsName.contains(".")
                    || parameter.getType() instanceof PsiArrayType) {
                // 兼容内部类，用$
                canonicalText = getJvmQualifiedClassName(parameter.getType());
            } else {
                canonicalText = "java.lang.Object";
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
                if (isNotJavaLang(topLevelFqn)) {
                    out.add(topLevelFqn);
                }
            } else {
                // 未解析的类型（例如源码未解析或依赖缺失）使用 canonicalText 作为回退，但同样过滤 java.lang
                String txt = type.getCanonicalText();
                if (isNotJavaLang(txt)) {
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
        if (isNotJavaLang(canon)) {
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
        if (isNotJavaLang(topLevelFqn)) {
            importNames = Collections.singleton(topLevelFqn);
        }
        String beanName = psiClass.getName();
        if (Objects.isNull(beanName)) {
            beanName = "bean_" + Integer.toHexString(Math.abs(UUID.randomUUID().toString().hashCode()));
        }
        return new ParamVo(getTypeName(psiClass), getParamName(beanName), importNames);
    }

    public static ParamVo getParamVo(PsiType psiType) {
        String paramName;
        if (psiType instanceof PsiArrayType) {
            paramName = getParamName(psiType.getDeepComponentType().getPresentableText());
        } else {
            paramName = getParamName(StringUtils.substringBefore(psiType.getPresentableText(), "<"));
        }
        return new ParamVo(getTypeName(psiType), paramName, collectImportsFromType(psiType));
    }

    /**
     * 获取安全的参数名称，处理Java关键字和JSON命名转换
     *
     * @param name 原始参数名
     * @return 处理后的安全参数名
     */
    public static String getParamName(@NotNull String name) {
        // 1. 首先进行JSON命名转换
        name = JsonUtil.convertName(name);
        // 2. 处理Java关键字冲突
        if (isJavaKeyword(name)) {
            // 关键字处理策略：添加下划线前缀
            return name + "_" + Integer.toHexString(Math.abs(UUID.randomUUID().toString().hashCode()));
        }
        return name;
    }

    /**
     * 检查字符串是否为Java关键字
     *
     * @param word 待检查的字符串
     * @return 如果是Java关键字返回true，否则返回false
     */
    private static boolean isJavaKeyword(String word) {
        return JAVA_KEYWORDS.contains(word);
    }

    /**
     * 保留泛型、内部类、数组、varargs等的类型文本
     */
    public static String getTypeName(PsiType type) {
        if (type instanceof PsiArrayType) {
            return getTypeName(((PsiArrayType) type).getComponentType()) + "[]";
        }
        // 通配符
        if (type instanceof PsiWildcardType) {
            PsiType bound = ((PsiWildcardType) type).getBound();
            if (Objects.isNull(bound)) {
                return "?";
            }
            if (((PsiWildcardType) type).isExtends()) {
                return "? extends " + getTypeName(bound);
            }
            if (((PsiWildcardType) type).isSuper()) {
                return "? super " + getTypeName(bound);
            }
        }
        String clsName = type.getCanonicalText();
        if (!ClassUtil.isPrimitive(clsName) && !clsName.contains(".")) {
            return "Object";
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
        return type.getPresentableText();
    }

    /**
     * 获取Class的类名
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

    private static boolean isNotJavaLang(String className) {
        return className != null && (!className.startsWith("java.lang.") || !className.matches("^java\\.lang(\\.[A-Za-z0-9]+)?$"));
    }
}
