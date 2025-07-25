package com.zj.runtimetest.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 时间工具类
 *
 * @author : jie.zhou
 * @date : 2025/7/25
 */
public class DateUtil {

    /**
     * 标准日期格式：yyyy-MM-dd
     */
    public final static String NORM_DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 标准时间格式：HH:mm:ss
     */
    public final static String NORM_TIME_PATTERN = "HH:mm:ss";

    /**
     * 标准日期时间格式，精确到分：yyyy-MM-dd HH:mm
     */
    public final static String NORM_DATETIME_MINUTE_PATTERN = "yyyy-MM-dd HH:mm";

    /**
     * 标准日期时间格式，精确到秒：yyyy-MM-dd HH:mm:ss
     */
    public final static String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 标准日期时间格式，精确到毫秒：yyyy-MM-dd HH:mm:ss.SSS
     */
    public final static String NORM_DATETIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    //-------------------------------------------------------------------------------------------------------------------------------- Pure
    /**
     * 标准日期格式：yyyyMMdd
     */
    public final static String PURE_DATE_PATTERN = "yyyyMMdd";

    /**
     * 标准日期格式：HHmmss
     */
    public final static String PURE_TIME_PATTERN = "HHmmss";

    /**
     * 标准日期格式：yyyyMMddHHmmss
     */
    public final static String PURE_DATETIME_PATTERN = "yyyyMMddHHmmss";

    /**
     * 标准日期格式：yyyyMMddHHmmssSSS
     */
    public final static String PURE_DATETIME_MS_PATTERN = "yyyyMMddHHmmssSSS";

    //-------------------------------------------------------------------------------------------------------------------------------- Others
    /**
     * UTC时间：yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    public final static String UTC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * 当前日期，格式 yyyy-MM-dd
     *
     * @return 当前日期的标准形式字符串
     */
    public static String today() {
        return formatDate(new Date());
    }

    // ------------------------------------ Format start ----------------------------------------------

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format 日期格式
     * @return 格式化后的字符串
     */
    public static String format(Date date, String format) {
        if (null == date || null == format || format.isEmpty()) {
            return null;
        }
        return new SimpleDateFormat(format).format(date);
    }


    /**
     * 格式化日期时间<br>
     * 格式 yyyy-MM-dd HH:mm:ss
     *
     * @param date 被格式化的日期
     * @return 格式化后的日期
     */
    public static String formatDateTime(Date date) {
        return format(date, NORM_DATETIME_PATTERN);
    }

    /**
     * 格式化日期部分（不包括时间）<br>
     * 格式 yyyy-MM-dd
     *
     * @param date 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date) {
        return format(date, NORM_DATE_PATTERN);
    }

    // ------------------------------------ Format end ----------------------------------------------

    // ------------------------------------ Parse start ----------------------------------------------

    /**
     * 将特定格式的日期转换为Date对象
     *
     * @param dateStr 特定格式的日期
     * @param format  格式，例如yyyy-MM-dd
     * @return 日期对象
     */
    public static Date parse(String dateStr, String format) {
        try {
            return new SimpleDateFormat(format).parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 格式yyyy-MM-dd HH:mm:ss
     *
     * @param dateString 标准形式的时间字符串
     * @return 日期对象
     */
    public static Date parseDateTime(String dateString) {
        dateString = normalize(dateString);
        return parse(dateString, NORM_DATETIME_PATTERN);
    }

    /**
     * 格式yyyy-MM-dd
     *
     * @param dateString 标准形式的日期字符串
     * @return 日期对象
     */
    public static Date parseDate(String dateString) {
        dateString = normalize(dateString);
        return parse(dateString, NORM_DATE_PATTERN);
    }

    /**
     * 解析时间，格式HH:mm:ss，默认为1970-01-01
     *
     * @param timeString 标准形式的日期字符串
     * @return 日期对象
     */
    public static Date parseTime(String timeString) {
        timeString = normalize(timeString);
        return parse(timeString, NORM_TIME_PATTERN);
    }

    /**
     * 解析时间，格式HH:mm:ss，日期默认为今天
     *
     * @param timeString 标准形式的日期字符串
     * @return 日期对象
     * @since 3.1.1
     */
    public static Date parseTimeToday(String timeString) {
        timeString = today() + " " + timeString;
        return parse(timeString, NORM_DATETIME_PATTERN);
    }

    /**
     * 解析UTC时间，格式为：yyyy-MM-dd'T'HH:mm:ss'Z
     *
     * @param utcString UTC时间
     * @return 日期对象
     * @since 4.1.14
     */
    public static Date parseUTC(String utcString) {
        return parse(utcString, UTC_PATTERN);
    }

    /**
     * 将日期字符串转换为{@link Date}对象，格式：<br>
     * <ol>
     * <li>yyyy-MM-dd HH:mm:ss</li>
     * <li>yyyy/MM/dd HH:mm:ss</li>
     * <li>yyyy.MM.dd HH:mm:ss</li>
     * <li>yyyy年MM月dd日 HH时mm分ss秒</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyy.MM.dd</li>
     * <li>HH:mm:ss</li>
     * <li>HH时mm分ss秒</li>
     * <li>yyyy-MM-dd HH:mm</li>
     * <li>yyyy-MM-dd HH:mm:ss.SSS</li>
     * <li>yyyyMMddHHmmss</li>
     * <li>yyyyMMddHHmmssSSS</li>
     * <li>yyyyMMdd</li>
     * <li>EEE, dd MMM yyyy HH:mm:ss z</li>
     * <li>EEE MMM dd HH:mm:ss zzz yyyy</li>
     * </ol>
     *
     * @param dateStr 日期字符串
     * @return 日期
     */
    public static Date parse(String dateStr) {
        if (null == dateStr) {
            return null;
        }
        // 去掉两边空格并去掉中文日期中的“日”，以规范长度
        dateStr = dateStr.trim().replace("日", "");
        int length = dateStr.length();
        try {
            Integer.valueOf(dateStr);
            // 纯数字形式
            if (length == PURE_DATETIME_PATTERN.length()) {
                return parse(dateStr, PURE_DATETIME_PATTERN);
            } else if (length == PURE_DATETIME_MS_PATTERN.length()) {
                return parse(dateStr, PURE_DATETIME_MS_PATTERN);
            } else if (length == PURE_DATE_PATTERN.length()) {
                return parse(dateStr, PURE_DATE_PATTERN);
            } else if (length == PURE_TIME_PATTERN.length()) {
                return parse(dateStr, PURE_TIME_PATTERN);
            }
        } catch (Exception ignored) {
        }

        if (length == NORM_DATETIME_PATTERN.length() || length == NORM_DATETIME_PATTERN.length() + 1) {
            if (dateStr.contains("T")) {
                // UTC时间格式：类似2018-09-13T05:34:31
                return parseUTC(dateStr);
            }
            return parseDateTime(dateStr);
        } else if (length == NORM_DATE_PATTERN.length()) {
            return parseDate(dateStr);
        } else if (length == NORM_TIME_PATTERN.length() || length == NORM_TIME_PATTERN.length() + 1) {
            return parseTimeToday(dateStr);
        } else if (length == NORM_DATETIME_MINUTE_PATTERN.length() || length == NORM_DATETIME_MINUTE_PATTERN.length() + 1) {
            return parse(normalize(dateStr), NORM_DATETIME_MINUTE_PATTERN);
        } else if (length >= NORM_DATETIME_MS_PATTERN.length() - 2) {
            return parse(normalize(dateStr), NORM_DATETIME_MS_PATTERN);
        }

        // 没有更多匹配的时间格式
        throw new RuntimeException("No format fit for date String [" + dateStr + "]");
    }

    // ------------------------------------ Parse end ----------------------------------------------

    /**
     * 标准化日期，默认处理以空格区分的日期时间格式，空格前为日期，空格后为时间：<br>
     * 将以下字符替换为"-"
     *
     * <pre>
     * "."
     * "/"
     * "年"
     * "月"
     * </pre>
     * <p>
     * 将以下字符去除
     *
     * <pre>
     * "日"
     * </pre>
     * <p>
     * 将以下字符替换为":"
     *
     * <pre>
     * "时"
     * "分"
     * "秒"
     * </pre>
     * <p>
     * 当末位是":"时去除之（不存在毫秒时）
     *
     * @param dateStr 日期时间字符串
     * @return 格式化后的日期字符串
     */
    private static String normalize(String dateStr) {
        if (null == dateStr || dateStr.isEmpty()) {
            return dateStr;
        }

        // 日期时间分开处理
        final List<String> dateAndTime = Arrays.stream(dateStr.split(" ")).collect(Collectors.toList());
        final int size = dateAndTime.size();
        if (size < 1 || size > 2) {
            // 非可被标准处理的格式
            return dateStr;
        }

        final StringBuilder builder = new StringBuilder();

        // 日期部分（"\"、"/"、"."、"年"、"月"都替换为"-"）
        String datePart = dateAndTime.get(0).replaceAll("[/.年月]", "-");
        if (datePart.endsWith("日")) {
            datePart = datePart.substring(0, datePart.length() - 1);
        }
        builder.append(datePart);

        // 时间部分
        if (size == 2) {
            builder.append(' ');
            String timePart = dateAndTime.get(1).replaceAll("[时分秒]", ":");
            if (timePart.endsWith(":")) {
                timePart = timePart.substring(0, timePart.length() - 1);
            }
            builder.append(timePart);
        }

        return builder.toString();
    }
}
