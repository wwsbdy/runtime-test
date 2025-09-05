package com.zj.runtimetest.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 序列化器
 * @author : jie.zhou
 * @date : 2025/7/25
 */
public interface Serializer {
    JsonSerializer<LocalDateTime> LOCAL_DATE_TIME_SERIALIZER = new JsonSerializer<LocalDateTime>() {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            ZoneId zoneId = ZoneId.systemDefault();
            // 转换为Date
            Date date = Date.from(value.atZone(zoneId).toInstant());
            gen.writeString(DateUtil.formatDateTime(date));
        }
    };
    JsonSerializer<LocalDate> LOCAL_DATE_SERIALIZER = new JsonSerializer<LocalDate>() {
        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            // 转换为Date
            Date date = Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant());
            gen.writeString(DateUtil.formatDate(date));
        }
    };
    JsonSerializer<Date> DATE_SERIALIZER = new JsonSerializer<Date>() {
        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            // 转换为Date
            gen.writeString(DateUtil.formatDateTime(value));
        }
    };
}
