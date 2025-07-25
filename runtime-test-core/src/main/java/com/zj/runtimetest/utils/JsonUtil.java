/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zj.runtimetest.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * @author 19242
 */
public class JsonUtil {

    public static ObjectMapper objectMapper = new ObjectMapper();

    private static class StringObjectMap extends HashMap<String, Object> {
    }

    static {
        // 注释处理
        objectMapper.configure(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature(), true);
        // 序列化处理
        objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        objectMapper.configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true);
        // 失败处理
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 单引号处理
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 防止中文乱码
        objectMapper.configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), false);
        // 启用美化输出
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, Deserializer.LOCAL_DATE_TIME_DESERIALIZER);
        javaTimeModule.addSerializer(LocalDateTime.class, Serializer.LOCAL_DATE_TIME_SERIALIZER);
        javaTimeModule.addDeserializer(LocalDate.class, Deserializer.LOCAL_DATE_DESERIALIZER);
        javaTimeModule.addSerializer(LocalDate.class, Serializer.LOCAL_DATE_SERIALIZER);
        javaTimeModule.addDeserializer(Date.class, Deserializer.DATE_DESERIALIZER);
        javaTimeModule.addSerializer(Date.class, Serializer.DATE_SERIALIZER);
        objectMapper.registerModule(javaTimeModule);
    }

    @SuppressWarnings("unchecked")
    public static <T> T toJavaBean(String content, Class<?> clazz) {
        if (Objects.isNull(content) || content.isEmpty()) {
            return (T) FiledUtil.getFieldNullValue(clazz);
        }
        try {
            JavaType javaType = JsonUtil.objectMapper.getTypeFactory().constructType(clazz);
            if (javaType.isTypeOrSubTypeOf(Temporal.class)) {
                content = "\"" + content + "\"";
            }
            return objectMapper.readValue(content, javaType);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Map<String, Object> toMap(String content) {
        try {
            return objectMapper.readValue(content, StringObjectMap.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T convertValue(Object content, Class<T> clazz) {
        if (content == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(content, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Object convertValue(Object content, Type type) {
        if (content == null) {
            return null;
        }
        try {
            JavaType javaType = JsonUtil.objectMapper.getTypeFactory().constructType(type);
            return objectMapper.convertValue(content, javaType);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static List<Map<String, Object>> toMaps(String content) {
        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            List<Map<String, Object>> result = new ArrayList<>();
            for (JsonNode node : jsonNode) {
                Map<String, Object> map = objectMapper.convertValue(node, StringObjectMap.class);
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String convertName(String name) {
        if (Objects.isNull(name) || name.isEmpty()) {
            return name;
        }
        // 首字母大写转小写
        if (Character.isUpperCase(name.charAt(0))) {
            name = name.substring(0, 1).toLowerCase() + name.substring(1);
        }
        return name;
    }

    public static boolean isJsonArray(String jsonString) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return jsonNode.isArray();
        } catch (Exception e) {
            return false;
        }
    }

    public static String toJsonString(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (Throwable e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
