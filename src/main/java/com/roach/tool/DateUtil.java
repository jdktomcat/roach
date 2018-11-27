package com.roach.tool;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 时间工具
 *
 * @author jdktomcat
 */
public class DateUtil {

    public static final String TIME_STR = "HH:mm:ss";
    public static final String DATE_STR = "yyyy-MM-dd";
    public static final String DATE_STR_FULL = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_STR_FILE = "yyyy-MM-dd-HH:mm:ss";


    /**
     * ----------------------日期格式化操作----------------------
     */
    public static String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_STR);
        return format.format(date);
    }

    public static String formatDateTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_STR_FULL);
        return format.format(date);
    }


    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * ----------------------日期转换操作----------------------
     */
    public static String timeStampToDate(long timeStamp, String formatStr) {
        Date date = new Date(timeStamp);
        SimpleDateFormat sd = new SimpleDateFormat(formatStr);
        return sd.format(date);
    }


    /**
     * Java将Unix时间戳转换成指定格式日期字符串
     */
    public static String unixTimeStampToDate(long timestamp, String formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        String date = sdf.format(new Date(timestamp * 1000));
        return date;
    }


    public static void main(String[] args) {
        System.out.println(timeStampToDate(1485710543, DATE_STR_FULL));
    }

}
