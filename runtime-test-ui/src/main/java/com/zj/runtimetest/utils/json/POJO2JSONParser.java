package com.zj.runtimetest.utils.json;

import com.google.common.collect.Sets;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.zj.runtimetest.constant.Constant;
import com.zj.runtimetest.utils.ParamUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Original work: POJO2JSONParser - Copyright (c) 2025 [organics2016]
 * Modifications/adjustments made by [jie.zhou] on [2025/6/30].
 * Key modifications:
 * 1. [去掉样例]
 * 2. [限制递归层数]
 * Source: <a href="https://github.com/organics2016/pojo2json">pojo2json</a>
 * License: [Apache 2.0]
 *
 * @date : 2025/7/2
 */
public class POJO2JSONParser {

    private static final Set<String> ITERABLE_TYPES = Sets.newHashSet("java.lang.Iterable",
            "java.util.Collection",
            "java.util.AbstractCollection",
            "java.util.List",
            "java.util.AbstractList",
            "java.util.Set",
            "java.util.AbstractSet");

    private static final Set<String> JAVA_OBJECT_TYPES = Sets.newHashSet(
            "java.lang.Boolean",
//            "java.lang.CharSequence",
            "java.lang.Character",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Number",
            "java.math.BigDecimal",
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.LocalTime",
            "java.time.YearMonth",
            "java.time.ZonedDateTime",
            "java.time.temporal.Temporal",
            "java.util.AbstractMap",
            "java.util.Date",
            "java.util.Map",
            "java.util.UUID");


    private static Object parseClass(PsiClass psiClass, int recursionLevel) {
        Map<String, Object> objectNode = new LinkedHashMap<>();
        List<Map.Entry<String, Object>> fields = Arrays.stream(psiClass.getAllFields())
                .map(field -> parseField(field, recursionLevel))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }
        for (Map.Entry<String, Object> field : fields) {
            objectNode.put(ParamUtil.convertName(field.getKey()), field.getValue());
        }
        return objectNode;
    }


    private static Map.Entry<String, Object> parseField(PsiField field, int recursionLevel) {
        // 移除所有 static 属性，这其中包括 kotlin 中的 companion object 和 INSTANCE
        if (field.hasModifierProperty(PsiModifier.STATIC)) {
            return null;
        }
        String fieldKey = field.getName();
        Object fieldValue = parseFieldValue(field.getType(), recursionLevel);
        return new AbstractMap.SimpleEntry<>(fieldKey, fieldValue);
    }


    public static Object parseFieldValue(PsiType type) {
        return parseFieldValue(type, 0);
    }

    /**
     * 解析字段值
     *
     * @param type           字段类型
     * @param recursionLevel 递归层级, 递归层级超过 4 层则不再递归
     * @return 解析后的字段值
     */
    private static Object parseFieldValue(PsiType type, int recursionLevel) {
        if (type instanceof PsiPrimitiveType) {

            return getPrimitiveTypeValue(type);

        } else if (type instanceof PsiArrayType) {

            PsiType typeToDeepType = ((PsiArrayType) type).getComponentType();
            Object obj = parseFieldValue(typeToDeepType, Constant.MAX_RECURSION_LEVEL + (recursionLevel == Constant.MAX_RECURSION_LEVEL ? 1 : 0));
            return obj != null ? Collections.singletonList(obj) : Collections.emptyList();

        } else {

            PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(type);

            if (psiClass == null) {
                return new LinkedHashMap<>();
            }
            if (psiClass.isEnum()) {
                return Arrays.stream(psiClass.getAllFields())
                        .filter(psiField -> psiField instanceof PsiEnumConstant)
                        .findFirst()
                        .map(PsiField::getName)
                        .orElse("");
            } else {
                Set<String> fieldTypeNames = new HashSet<>();
                fieldTypeNames.add(psiClass.getQualifiedName());
                fieldTypeNames.addAll(Arrays.stream(psiClass.getSupers()).map(PsiClass::getQualifiedName).collect(Collectors.toSet()));
                fieldTypeNames = fieldTypeNames.stream().filter(Objects::nonNull).collect(Collectors.toSet());
                boolean iterable = fieldTypeNames.stream().anyMatch(ITERABLE_TYPES::contains);
                if (iterable) {
                    PsiType typeToDeepType = PsiUtil.extractIterableTypeParameter(type, false);
                    if (typeToDeepType == null) {
                        return Collections.emptyList();
                    }
                    if (recursionLevel > Constant.MAX_RECURSION_LEVEL) {
                        return Collections.emptyList();
                    }
                    Object obj = parseFieldValue(typeToDeepType, Constant.MAX_RECURSION_LEVEL + (recursionLevel == Constant.MAX_RECURSION_LEVEL ? 1 : 0));
                    return obj != null ? Collections.singletonList(obj) : Collections.emptyList();
                } else {
                    if (isCharSequence(fieldTypeNames)) {
                        return "";
                    }
                    if (isJavaObject(psiClass.getQualifiedName())) {
                        return null;
                    }
                    if (recursionLevel > Constant.MAX_RECURSION_LEVEL) {
                        return null;
                    }
                    PsiClass subPsiClass = PsiUtil.resolveClassInClassTypeOnly(type);
                    if (subPsiClass != null) {
                        return parseClass(subPsiClass, recursionLevel + 1);
                    }
                    return Collections.emptyMap();
                }
            }
        }
    }

    private static boolean isJavaObject(String className) {
        if (Objects.isNull(className) || className.isEmpty()) {
            return false;
        }
        return !"java.lang.Object".equals(className)
                && className.startsWith("java.")
                || className.startsWith("sum.")
                || JAVA_OBJECT_TYPES.contains(className);
    }

    private static boolean isCharSequence(Collection<String> fieldTypeNames) {
        return fieldTypeNames.contains("java.lang.CharSequence");
    }

    private static Map<String, PsiType> getPsiClassGenerics(PsiType type) {
        PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(type);
        if (psiClass != null) {
            return Arrays.stream(psiClass.getTypeParameters()).map(p -> Pair.of(p, PsiUtil.substituteTypeParameter(type, psiClass, p.getIndex(), false))).filter(p -> p.getValue() != null).collect(Collectors.toMap(p -> p.getKey().getName(), Pair::getValue));
        }
        return Collections.emptyMap();
    }

    private static Object getPrimitiveTypeValue(PsiType type) {

        switch (type.getCanonicalText()) {
            case "boolean":
                return false;
            case "byte":
            case "short":
            case "int":
            case "long":
                return 0;
            case "float":
                return 0F;
            case "double":
                return 0D;
            case "char":
                return '0';
            default:
                return null;
        }
    }

}
