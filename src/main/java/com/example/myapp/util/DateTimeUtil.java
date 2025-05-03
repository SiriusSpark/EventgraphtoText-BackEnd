package com.example.myapp.util;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class DateTimeUtil {
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy-MM"),
        DateTimeFormatter.ofPattern("yyyy")
    );

    /**
     * 解析日期时间字符串为LocalDateTime对象
     * 支持多种格式：
     * - yyyy-MM-dd HH:mm:ss
     * - yyyy-MM-dd'T'HH:mm:ss
     * - yyyy-MM-dd
     * - yyyy-MM
     * - yyyy
     * 
     * @param dateStr 日期时间字符串，可以为null或空
     * @return LocalDateTime对象，如果输入为null或空字符串则返回null
     */
    public static LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        // 移除年份前的前导零
        dateStr = removeLeadingZerosFromYear(dateStr);

        // 尝试所有支持的格式
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                // 对于只有日期的格式，补充时间部分
                if (dateStr.length() <= 10) {
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    return date.atStartOfDay();
                } else {
                    return LocalDateTime.parse(dateStr, formatter);
                }
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
                continue;
            }
        }

        throw new IllegalArgumentException("不支持的日期时间格式: " + dateStr);
    }

    /**
     * 移除年份部分的前导零
     * 例如: "0266" -> "266"
     */
    private static String removeLeadingZerosFromYear(String dateStr) {
        // 查找年份部分（第一个连字符之前的部分）
        int hyphenIndex = dateStr.indexOf('-');
        if (hyphenIndex == -1) {
            // 如果只有年份
            return dateStr.replaceFirst("^0+(?!$)", "");
        }
        
        // 分离年份和剩余部分
        String year = dateStr.substring(0, hyphenIndex);
        String rest = dateStr.substring(hyphenIndex);
        
        // 移除年份的前导零
        year = year.replaceFirst("^0+(?!$)", "");
        
        return year + rest;
    }

    /**
     * 格式化LocalDateTime为字符串
     * 根据时间的精确度返回不同格式：
     * - 如果时间部分为00:00:00，则只返回日期部分
     * - 如果日和月都是1号，则只返回年份
     * - 如果日是1号，则只返回年月
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        if (dateTime.getHour() == 0 && dateTime.getMinute() == 0 && dateTime.getSecond() == 0) {
            // 时间部分都是0，只返回日期部分
            if (dateTime.getMonthValue() == 1 && dateTime.getDayOfMonth() == 1) {
                // 如果是1月1日，只返回年份
                return String.valueOf(dateTime.getYear());
            } else if (dateTime.getDayOfMonth() == 1) {
                // 如果是1号，返回年月
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            } else {
                // 返回完整日期
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        }

        // 返回完整的日期时间
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    /**
     * 转换为Neo4j日期时间格式
     */
    public static String toNeo4jDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
} 