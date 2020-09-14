package org.serverct.parrot.parrotx.utils;

import lombok.NonNull;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtil {

    private static final int YEAR = 365 * 24 * 60 * 60; // one year's int data
    private static final int MONTH = 30 * 24 * 60 * 60; // one month's int data
    private static final int DAY = 24 * 60 * 60; // one day's int data
    private static final int HOUR = 60 * 60; // one hour int data
    private static final int MINUTE = 60; // one minute int data
    public static final Map<String, Integer> DEFAULT_TIME_KEY = new LinkedHashMap<String, Integer>() {{
        put("年", YEAR);
        put("月", MONTH);
        put("天", DAY);
        put("时", HOUR);
        put("分", MINUTE);
        put("秒", 1);
    }};
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String CHINESE_DATE_FORMAT = "yyyy年MM月dd日 HH:mm:ss";
    private final static Pattern PATTERN = Pattern.compile("(\\d+[yMdhms])+");
    private final static Pattern TYPE = Pattern.compile("([yMdhms])");

    // Prevent accidental construction
    private TimeUtil() {
    }

    /**
     * 从描述性时间长度字符串中获取以秒为单位的时间长度。
     * @param duration 描述性时间长度，例如 19d19h810m
     * @return 以秒为单位的时间长度数字
     */
    public static long getTime(String duration) {
        long result = 0;
        Matcher matcher = PATTERN.matcher(duration);
        while (matcher.find()) {
            final String part = matcher.group(1);
            final Matcher typeMatcher = TYPE.matcher(part);
            if (!typeMatcher.find()) {
                continue;
            }
            final String type = typeMatcher.group();
            final long time = Long.parseLong(part.replace(type, ""));
            switch (type) {
                case "y":
                    result += time * YEAR;
                    break;
                case "M":
                    result += time * MONTH;
                    break;
                case "d":
                    result += time * DAY;
                    break;
                case "h":
                    result += time * HOUR;
                    break;
                case "m":
                    result += time * MINUTE;
                    break;
                case "s":
                    result += time;
                    break;
            }
            duration = duration.replace(part, "");
            matcher = PATTERN.matcher(duration);
        }
        return result;
    }

    /**
     * 根据数字（单位：秒）获取描述性时间长度字符串，如 1 年 1 月 4 天 5 小时 1 分钟 4 秒
     *
     * @param time       需要格式化的秒数
     * @param timeKey    单位时间长短及其称呼
     * @param format     单位时间的展示格式
     * @param ignoreZero 是否忽略 0
     * @return 时间长度
     */
    public static String getTimeLong(int time, final Map<String, Integer> timeKey, final String format, final boolean ignoreZero) {
        int cache;
        final StringBuilder result = new StringBuilder();

        if (time <= 0) {
            return MessageFormat.format(format, time, "秒");
        }

        for (Map.Entry<String, Integer> entry : timeKey.entrySet()) {
            final String key = entry.getKey();
            final int value = entry.getValue();

            cache = time % value;
            final int number = (time - cache) / value;
            time -= number * value;

            if (!ignoreZero || number != 0) {
                result.append(MessageFormat.format(format, number, key));
            }
        }
        return result.toString().trim();
    }

    public static String getTimeLong(int time, final String format, final boolean ignoreZero) {
        return getTimeLong(time, DEFAULT_TIME_KEY, format, ignoreZero);
    }

    public static String getTimeLong(int time, final String format) {
        return getTimeLong(time, DEFAULT_TIME_KEY, format, true);
    }

    public static String getTimeLong(int time) {
        return getTimeLong(time, DEFAULT_TIME_KEY, "{0} {1} ", true);
    }


    /**
     * 根据时间戳获取描述性时间，如3分钟前，1天前
     *
     * @param timestamp 时间戳 单位为毫秒
     * @return 时间字符串
     */
    public static String getDescriptionTimeFromTimestamp(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeGap = (currentTime - timestamp) / 1000;// 与现在时间相差秒数
        String timeStr;
        if (timeGap > YEAR) {
            timeStr = timeGap / YEAR + "年前";
        } else if (timeGap > MONTH) {
            timeStr = timeGap / MONTH + "个月前";
        } else if (timeGap > DAY) {// 1天以上
            timeStr = timeGap / DAY + "天前";
        } else if (timeGap > HOUR) {// 1小时-24小时
            timeStr = timeGap / HOUR + "小时前";
        } else if (timeGap > MINUTE) {// 1分钟-59分钟
            timeStr = timeGap / MINUTE + "分钟前";
        } else {// 1秒钟-59秒钟
            timeStr = "刚刚";
        }
        return timeStr;
    }

    /**
     * 判断今日是否为休假日
     * <p>
     * check today is week
     *
     * @return true -> yes / false -> no
     */
    public static boolean isWeekDay() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    /**
     * 日期格式化
     * <p>
     * format a date
     *
     * @param date             dateObj
     * @param simpleDataFormat simpleDataFormatObj
     * @return formattedDateString
     */
    public static String getFormattedDate(Date date, @NonNull SimpleDateFormat simpleDataFormat) {
        return simpleDataFormat.format(date);
    }

    /**
     * 日期格式化，默认日期格式 yyyy-MM-dd
     * <p>
     * format a date, use yyyy-MM-dd
     *
     * @param date dateObj
     * @return formattedDateString
     */
    public static String getDefaultFormatDate(Date date) {
        return getFormattedDate(date, new SimpleDateFormat(DEFAULT_DATE_FORMAT));
    }

    /**
     * 日期格式化，默认日期格式 yyyy年MM月dd日
     * <p>
     * format a date, use yyyy年MM月dd日
     *
     * @param date dateObj
     * @return formattedDateString
     */
    public static String getChineseDateFormat(Date date) {
        return getFormattedDate(date, new SimpleDateFormat(CHINESE_DATE_FORMAT));
    }
}
