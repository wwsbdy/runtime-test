package com.zj.runtimetest.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * 反序列化器
 * @author : jie.zhou
 * @date : 2025/7/25
 */
public interface Deserializer {
    JsonDeserializer<LocalDateTime> LOCAL_DATE_TIME_DESERIALIZER = new JsonDeserializer<LocalDateTime>() {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (Objects.isNull(text) || text.isEmpty()) {
                return null;
            }
            Date date = DateUtil.parse(text);
            if (Objects.isNull(date)) {
                return null;
            }
            return date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }
    };
    JsonDeserializer<LocalDate> LOCAL_DATE_DESERIALIZER = new JsonDeserializer<LocalDate>() {
        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (Objects.isNull(text) || text.isEmpty()) {
                return null;
            }
            Date date = DateUtil.parse(text);
            if (Objects.isNull(date)) {
                return null;
            }
            // 转换为LocalDate
            return date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
    };
    JsonDeserializer<Date> DATE_DESERIALIZER = new JsonDeserializer<Date>() {
        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (Objects.isNull(text) || text.isEmpty()) {
                return null;
            }
            return DateUtil.parse(text);
        }
    };
}
