/*
 * Copyright 2018 The Higgs Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vilada.higgs.data.web.service.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.common.base.Strings;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by yawei on 2017-6-29.
 */
@Slf4j
public class DateUtil {
    public static final String patternYMD = "yyyy-MM-dd";
    public static final String patternYMDHMS = "yyyy-MM-dd HH:mm:ss";
    public static final String patternYMDHM = "yyyy-MM-dd HH:mm";
    public static final String patternYMD_HMS = "yyyy-MM-dd_HH:mm:ss";


    public static long addDate(Calendar calender, int month, int day, int hour, int minute){
        calender.add(Calendar.MONTH, month);
        calender.add(Calendar.DATE, day);
        calender.add(Calendar.HOUR_OF_DAY, hour);
        calender.add(Calendar.MINUTE, minute);
        return calender.getTimeInMillis();
    }
    public static long setDate(Calendar calender, int month, int day, int hour, int minute, int second){
        calender.set(Calendar.MONTH, month);
        calender.set(Calendar.DATE, day);
        calender.set(Calendar.HOUR_OF_DAY, hour);
        calender.set(Calendar.MINUTE, minute);
        calender.set(Calendar.SECOND, second);
        return calender.getTimeInMillis();
    }
    public static long addMonth(Calendar calender, int month){
        calender.add(Calendar.MONTH, month);
        return calender.getTimeInMillis();
    }
    public static long addDay(Calendar calender, int day){
        calender.add(Calendar.DATE, day);
        return calender.getTimeInMillis();
    }
    public static long addHour(Calendar calender, int hour){
        calender.add(Calendar.HOUR_OF_DAY, hour);
        return calender.getTimeInMillis();
    }
    public static long addMinute(Calendar calender, int minute){
        calender.add(Calendar.MINUTE, minute);
        return calender.getTimeInMillis();
    }
    public static long addMillisecond(Calendar calender, int millisecond){
        calender.add(Calendar.MILLISECOND, millisecond);
        return calender.getTimeInMillis();
    }

    public static String createDateTime(long time, Long interval, String format){
        Calendar calender = Calendar.getInstance();
        calender.setTimeInMillis(time);
        Date date = new Date(createDateTime(calender, interval));
        return formatDate(date, format);
    }
    
    public static long createDateTime(long time, Long interval){
        Calendar calender = Calendar.getInstance();
        calender.setTimeInMillis(time);
        return createDateTime(calender, interval);
    }
    
    public static long createDateTime(Calendar calender, Long interval){
        return DateUtil.addMillisecond(calender, interval.intValue());
    }

    public static String formatDate(Date date, String format){
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 将时间戳转换为时间
     * @param s String类型的时间
     * @return
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * 将时间转换为时间戳
     *
     * @param s String类型的时间
     * @return
     * @throws ParseException
     */
    public static String dateToStamp(String s) throws ParseException{
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

    /**
     * 将String格式的日期转换为Long类型
     *
     * @param dateStr String类型的日期
     * @param pattern 日期格式
     * @return 时间转换后对应的Long值
     */
    public static Long transferStringDateToLong(String dateStr, String pattern) {
        if (Strings.isNullOrEmpty(dateStr) || Strings.isNullOrEmpty(pattern)) {
            return 0L;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date date;
        try {
            date = simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            log.error("parse String type date {} by pattern {} failed ", dateStr, pattern);
            return 0L;
        }

        return date.getTime();
    }

    /**
     * 将String格式的日期转换为Long类型,默认pattern为"yyyy-MM-dd HH:mm:ss"
     *
     * @param dateStr String类型的日期
\     * @return 时间转换后对应的Long值
     */
    public static Long transferStringDateToLong(String dateStr) {
        return transferStringDateToLong(dateStr, patternYMDHMS);
    }

}
