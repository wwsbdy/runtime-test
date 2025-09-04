package com.zj.runtimetest.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Json工具类 (基于 Gson)
 * @author
 */
public class JsonUtil {

    private static final Gson gson;

    static {
        // TODO 待完善 父子类重复字段报错，时间反序列报错
        // 自定义 Gson 配置（美化输出、防止转义等）
        gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        Class<?> declaringClass = f.getDeclaringClass();
                        String fieldName = f.getName();

                        // 获取当前类的子类字段集合
                        Set<String> subclassFields = getDeclaredFieldNamesRecursively(declaringClass);

                        // 如果父类字段在子类也存在，跳过父类字段
                        return !declaringClass.equals(f.getDeclaringClass())
                                && subclassFields.contains(fieldName);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }

                    /**
                     * 获取类及其所有子类的字段名
                     */
                    private Set<String> getDeclaredFieldNamesRecursively(Class<?> clazz) {
                        Set<String> names = new HashSet<>();
                        for (Field field : clazz.getDeclaredFields()) {
                            names.add(field.getName());
                        }
                        Class<?> superclass = clazz.getSuperclass();
                        if (superclass != null && superclass != Object.class) {
                            names.addAll(getDeclaredFieldNamesRecursively(superclass));
                        }
                        return names;
                    }
                })
                .serializeNulls()
                .setPrettyPrinting() // 美化输出
                .disableHtmlEscaping() // 防止中文被转义
                // LocalDateTime 序列化/反序列化
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                                LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                // LocalDate 序列化/反序列化
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                                LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
                .create();
    }

    /**
     * JSON 转 JavaBean
     */
    public static <T> T toJavaBean(String content, Class<T> clazz) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(content, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * JSON 转 Map
     */
    public static Map<String, Object> toMap(String content) {
        try {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            return gson.fromJson(content, type);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 对象转指定类型
     */
    public static <T> T convertValue(Object content, Class<T> clazz) {
        if (content == null) {
            return null;
        }
        try {
            String json = gson.toJson(content);
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Object convertValue(Object content, Type type) {
        if (content == null) {
            return null;
        }
        try {
            String json = gson.toJson(content);
            return gson.fromJson(json, type);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * JSON 转 List<Map<String, Object>>
     */
    public static List<Map<String, Object>> toMaps(String content) {
        try {
            Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
            return gson.fromJson(content, type);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 首字母转小写
     */
    public static String convertName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (Character.isUpperCase(name.charAt(0))) {
            return name.substring(0, 1).toLowerCase() + name.substring(1);
        }
        return name;
    }

    /**
     * 是否是 JSON 数组
     */
    public static boolean isJsonArray(String jsonString) {
        try {
            JsonElement element = JsonParser.parseString(jsonString);
            return element.isJsonArray();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJsonString(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else {
            try {
                return gson.toJson(value);
            } catch (Throwable e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
