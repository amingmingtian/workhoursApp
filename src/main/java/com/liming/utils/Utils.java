package com.liming.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static final SimpleDateFormat Y2_DATE_FORMATTER = new SimpleDateFormat("yy-MM-dd");
    public static final SimpleDateFormat Y4_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm");


    public static boolean isValidDate(String str) {
        boolean validFlag = true;
        try {
            Y4_DATE_FORMATTER.setLenient(false);
            Y4_DATE_FORMATTER.parse(str);
        } catch (ParseException e) {
            validFlag = false;
        }
        return validFlag;
    }


    public static boolean isValidTime(String str) {
        boolean validFlag = true;
        try {
            TIME_FORMATTER.setLenient(false);
            TIME_FORMATTER.parse(str);
        } catch (ParseException e) {
            validFlag = false;
        }
        return validFlag;
    }

    public static Date parseDateY4(String dateStr) {
        Date date = null;
        try {
            Y4_DATE_FORMATTER.setLenient(false);
            date = Y4_DATE_FORMATTER.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date parseDateY2(String dateStr) {
        Date date = null;
        try {
            Y2_DATE_FORMATTER.setLenient(false);
            date = Y2_DATE_FORMATTER.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public static Date extractAndParseTime(String timeStr) {
        String timeRegStr = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
        Pattern pattern = Pattern.compile(timeRegStr);
        Matcher m = pattern.matcher(timeStr);
        String timeStrHHMM = null;
        if (m.find()) {
            timeStrHHMM = m.group();
        }

        return parseTime(timeStrHHMM);
    }

    public static Date parseTime(String timeStr) {

        Date time = null;
        try {
            TIME_FORMATTER.setLenient(false);
            time = TIME_FORMATTER.parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public static String getTimeStr(Date time) {
        return TIME_FORMATTER.format(time);
    }

    public static String getDateY4Str(Date time) {
        return Y4_DATE_FORMATTER.format(time);
    }

    public static float calculateIntervalHours(Date time1, Date time2) {

        float interval = (float) (time2.getTime() - time1.getTime()) / (3600 * 1000);
        if (time2.before(time1)) {
            interval = interval + 24;
        }
        return interval;
    }

    public static List<Date> listAllDates(Date start, Date end) {
        List<Date> list = new ArrayList<>();
        long firstDay = start.getTime();
        long lastDay = end.getTime();

        Long oneDay = 1000 * 60 * 60 * 24L;

        long someday = firstDay;
        while (someday <= lastDay) {
            list.add(new Date(someday));
            someday += oneDay;
        }
        return list;
    }

    public static String getDateListStr(List<Date> dateList) {

        if (dateList.isEmpty()) {
            return "[] : empty date list, 0 elements!";
        }
        StringBuilder sb = new StringBuilder("[");
        for (Date date : dateList) {
            sb.append(Utils.getDateY4Str(date) + ",");
        }
        sb.setLength(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    public static boolean isAfternoon(Date time) {
        if (time == null) {
            return false;
        }

        return time.getHours() >= 12;

    }
}
