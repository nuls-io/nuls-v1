/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.core.tools.date;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 时间工具
 *
 * @author ln
 */
public class DateUtil {

    public final static String EMPTY_SRING = "";
    public final static String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";


    public final static long DATE_TIME = 1000 * 24 * 60 * 60;
    public final static long HOUR_TIME = 1000 * 60 * 60;
    public final static long MINUTE_TIME = 1000 * 60;
    public final static long SECEND_TIME = 1000;

    public static String toGMTString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.UK);
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }

    //    /**
//     * 把日期转换成yyyy-MM-dd HH:mm:ss格式
//     *
//     * @param date
//     * @return String
//     */
    public static String convertDate(Date date) {
        if (date == null) {
            return EMPTY_SRING;
        }
        return new SimpleDateFormat(DEFAULT_PATTERN).format(date);
    }

    //    /**
//     * 把日期转换成pattern格式
//     *
//     * @param date
//     * @param pattern
//     * @return String
//     */
    public static String convertDate(Date date, String pattern) {
        if (date == null) {
            return EMPTY_SRING;
        }
        return new SimpleDateFormat(pattern).format(date);
    }

    //    /**
//     * @param date
//     * @return Date
//     */
    public static Date convertStringToDate(String date) {
        try {
            return new SimpleDateFormat(DEFAULT_PATTERN).parse(date);
        } catch (ParseException e) {
        }
        return new Date();
    }

    //    /**
//     * @param date
//     * @param pattern
//     * @return Date
//     */
    public static Date convertStringToDate(String date, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    //
//    /**
//     * 判断传入的日期是不是当月的第一天
//     *
//     * @param date
//     * @return boolean
//     */
    public static boolean isFirstDayInMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    //    /**
//     * 判断传入的日期是不是当年的第一天
//     *
//     * @param date
//     * @return boolean
//     */
    public static boolean isFirstDayInYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_YEAR) == 1;
    }

    //    /**
//     * 去点时分秒后返回
//     *
//     * @param date
//     * @return Date
//     */
    public static Date rounding(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    //    /**
//     * 把时间加上day天后返回，如果传负数代表减day天
//     *
//     * @param date
//     * @param day
//     * @return Date
//     */
    public static Date dateAdd(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + day);
        return calendar.getTime();
    }

    //    /**
//     * 多少个月前后的今天
//     *
//     * @param date
//     * @param month
//     * @return Date
//     */
    public static Date dateAddMonth(Date date, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + month);
        return calendar.getTime();
    }

    //    /**
//     * 获取上一个月的第一天
//     *
//     * @return Date
//     */
    public static Date getFirstDayOfPreviousMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    //    /**
//     * 获取本月的第一天
//     *
//     * @return Date
//     */
    public static Date getFirstDayOfMonth() {
        return getFirstDayOfMonth(new Date());
    }

    //    /**
//     * 获取本月的第一天
//     * @param date
//     * @return Date
//     */
    public static Date getFirstDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    //    /**
//     * 获取上一年的第一天
//     *
//     * @return Date
//     */
    public static Date getFirstDayOfPreviousYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static List<String> getDateRange(String beginDate, String endDate,
                                            int type) {
        List<String> list = new ArrayList<String>();
        if (isEmpty(beginDate) || isEmpty(endDate)) {
            return list;
        }
        if (type == 1) {
            Date begin = convertStringToDate(beginDate, "yyyy-MM-dd");
            Date end = convertStringToDate(endDate, "yyyy-MM-dd");
            if (begin == null || end == null) {
                return list;
            }
            while (begin.equals(end) || begin.before(end)) {
                list.add(convertDate(begin, "MM-dd"));
                begin = dateAdd(begin, 1);
            }
        } else if (type == 2) {
            Date begin = convertStringToDate(beginDate, "yyyy-MM-dd");
            Date end = convertStringToDate(endDate, "yyyy-MM-dd");
            if (begin == null || end == null) {
                return list;
            }
            Calendar beginCalendar = Calendar.getInstance();
            beginCalendar.setTime(begin);
            beginCalendar.set(Calendar.DAY_OF_MONTH, 1);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(end);
            endCalendar.set(Calendar.DAY_OF_MONTH, 1);

            while (beginCalendar.getTime().equals(endCalendar.getTime())
                    || beginCalendar.getTime().before(endCalendar.getTime())) {
                list.add(convertDate(beginCalendar.getTime(), "yyyy-MM"));
                beginCalendar.set(Calendar.MONTH,
                        beginCalendar.get(Calendar.MONTH) + 1);
            }
        }
        return list;
    }

    public static boolean isEmpty(Object obj) {
        return obj == null || EMPTY_SRING.equals(obj);
    }

    //    /**
//     * 获取星期几
//     *
//     * @param c
//     * @return String
//     */
    public static String getWeekDay(Calendar c) {
        if (c == null) {
            return "星期一";
        }
        if (Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期一";
        }
        if (Calendar.TUESDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期二";
        }
        if (Calendar.WEDNESDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期三";
        }
        if (Calendar.THURSDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期四";
        }
        if (Calendar.FRIDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期五";
        }
        if (Calendar.SATURDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期六";
        }
        if (Calendar.SUNDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期日";
        }
        return "星期一";
    }


    public static Date getToday() {
        return rounding(new Date());
    }

    public static Date getYesterday() {
        return rounding(dateAdd(new Date(), -1));
    }

    //    /**
//     * 获取两个日期之间的间隔天数
//     *
//     * @param startTime
//     * @param endTime
//     * @return int
//     */
    public static int getBetweenDateDays(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        long to = startTime.getTime();
        long from = endTime.getTime();

        return (int) ((from - to) / (1000L * 60 * 60 * 24));
    }

    public static Date getTomorrow() {
        return rounding(dateAdd(new Date(), 1));
    }

    //    /**
//     * 检查传入的时间是否在当前时间小时数之后
//     *
//     * @param date
//     * @param time
//     * @return boolean
//     */
    public static boolean checkAfterTime(Date date, String time) {
        Date dateTime = convertStringToDate(convertDate(date, "yyyy-MM-dd").concat(" ").concat(time));
        return dateTime.before(date);
    }

    public static String getOffsetStringDate(long offsetTime) {
        int p = offsetTime > 0 ? 1 : -1;

        offsetTime = Math.abs(offsetTime);
        if (offsetTime < 1000) {
            return p * offsetTime + "ms";
        } else if (offsetTime < MINUTE_TIME) {
            long sec = offsetTime % DATE_TIME % HOUR_TIME % MINUTE_TIME / 1000;
            return p * sec + "s";
        } else if (offsetTime < HOUR_TIME) {
            long minute = offsetTime % DATE_TIME % HOUR_TIME / MINUTE_TIME;
            long sec = offsetTime % DATE_TIME % HOUR_TIME % MINUTE_TIME / 1000;
            if (minute >= 10) {
                return p * minute + "m";
            } else {
                return p * minute + "m" + sec + "s";
            }
        } else {
            long hour = offsetTime % DATE_TIME / HOUR_TIME;
            long minute = offsetTime % DATE_TIME % HOUR_TIME / MINUTE_TIME;
            if (hour >= 5) {
                return p * hour + "h";
            } else {
                return p * hour + "h" + minute + "m";
            }
        }
    }

}
