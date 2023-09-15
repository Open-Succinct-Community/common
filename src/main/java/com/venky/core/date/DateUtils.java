package com.venky.core.date;

import com.venky.cache.UnboundedCache;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class DateUtils { // NOPMD by VMahadevan on 1/26/09 11:16 PM
    private static final ThreadLocal<Map<String,SimpleDateFormat>> dateFormatFactory = new ThreadLocal<>();

    public static long compareToMillis(final Date d1, final Date d2) {
        return compareToMillis(d1.getTime(), d2.getTime());
    }

    public static int compareToMinutes(final Date d1, final Date d2) {
        return compareToMinutes(d1.getTime(), d2.getTime());
    }
    public static double compareToDMinutes(final Date d1, final Date d2) {
        return compareToDMinutes(d1.getTime(), d2.getTime());
    }
    public static long compareToMillis(final long millis1, final long millis2) {
        return (millis1 - millis2);
    }
    public static int compareToMinutes(final long millis1, final long millis2) {
    	return (int) Math.round(compareToDMinutes(millis1, millis2));
    }
    public static double compareToDMinutes(final long millis1, final long millis2) {
        final long millisecondDiff = compareToMillis(millis1, millis2);

        final double millisecondsInMinute = 60 * 1000.0;

        final double diff = (millisecondDiff) / millisecondsInMinute;
        return diff;
    }

    public static Date addHours(final Date to, final int hours) {
        return new Date(addHours(to.getTime(), hours));
    }

    public static Date addMinutes(final Date to, final int minutes) {
        return new Date(addMinutes(to.getTime(), minutes));
    }

    public static long addHours(final long to, final int hours) {
        return (to + hours * 3600000L);
    }

    public static long addMinutes(final long to, final int minutes) {
        return (to + minutes * 60000L);
    }

    public static long addSeconds(final long to, final int seconds) {
        return (to + seconds * 1000L);
    }

    public static long addMillis(final long to, final long millis) {
        return (to + millis);
    }

    public static Date addToDate(final Date to, final int field, final int amount) {
        return new Date(addToMillis(to.getTime(), field, amount));
    }

    public static long addToMillis(final long to, final int field, final int amount) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(to);
        cal.add(field, amount);
        return cal.getTimeInMillis();
    }

    public static final String ISO_TIME_FORMAT_STR = "HH:mm:ss";
    
    public static final String ISO_DATE_FORMAT_STR = "yyyy-MM-dd";

    public static final String ISO_DATE_TIME_FORMAT_STR = "yyyy-MM-dd HH:mm:ss";

    public static final String ISO_DATE_TIME_FORMAT_WITH_MILLIS_STR = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    
    public static final String APP_TIME_FORMAT_STR = "HH:mm";
    
    public static final String APP_TIME_FORMAT_WITH_TZ_STR = "HH:mm Z";
    
    public static final String APP_DATE_TIME_FORMAT_STR = "dd/MM/yyyy HH:mm:ss";
    
    public static final String APP_DATE_TIME_FORMAT_WITH_TZ_STR = "dd/MM/yyyy HH:mm:ss Z";
    
    public static final String APP_DATE_FORMAT_STR = "dd/MM/yyyy";
    
    public static final Date HIGH_DATE = getHighDate();

    public static DateFormat getFormat(String fmt){
        if (dateFormatFactory.get() == null){
            dateFormatFactory.set(new UnboundedCache<String, SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat getValue(String fmt) {
                    return new SimpleDateFormat(fmt,Locale.getDefault());
                }
            });
        }
        return dateFormatFactory.get().get(fmt);
    }
    private static Date getHighDate() {
        try {
            return getFormat(APP_DATE_TIME_FORMAT_STR).parse("31/12/2999 12:59:59"); // NOPMD by VMahadevan on 1/26/09 11:12 PM
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Date getStartOfDay(final Date date) {
        return new Date(getStartOfDay(date.getTime()));
    }

    public static long getStartOfDay(final long date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();

    }

    public static long getEndOfDay(final long date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public static Date getEndOfDay(final Date date) {
        return new Date(getEndOfDay(date.getTime()));
    }

    public static Date getTimeOfDay(final Date date, final String atTimeofDay) {
        return getTimeOfDay(date, new Time(atTimeofDay));
    }

    public static Date getTimeOfDay(final Date date, final Time atTimeofDay) {
        return new Date(getTimeOfDay(date.getTime(), atTimeofDay));
    }

    public static long getTimeOfDay(final long date, final Time atTimeofDay) {
        try {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(date);

            calendar.set(Calendar.HOUR_OF_DAY, atTimeofDay.getHours());
            calendar.set(Calendar.MINUTE, atTimeofDay.getMinutes());
            calendar.set(Calendar.SECOND, atTimeofDay.getSeconds());

            return calendar.getTimeInMillis();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static Date getDate(final String dateStr) {
    	String[] trialFormats = new String[] { APP_DATE_TIME_FORMAT_WITH_TZ_STR , APP_DATE_TIME_FORMAT_STR, ISO_DATE_TIME_FORMAT_WITH_MILLIS_STR, ISO_8601_24H_FULL_FORMAT, ISO_DATE_TIME_FORMAT_STR, APP_DATE_FORMAT_STR, ISO_DATE_FORMAT_STR };
    	for (String trialFormat : trialFormats ){
    			try {
					return getFormat(trialFormat).parse(dateStr);
				} catch (ParseException e) {
					//e.printStackTrace();
				}
    	}

        try {
        	long time = Long.parseLong(dateStr);
        	return new Date(time);
        }catch (NumberFormatException ex){
        }
        throw new RuntimeException("Unknown Date Format : " + dateStr);
    }

    public static Date getTime(String time) {
    	String[] trialFormats = new String[] { APP_TIME_FORMAT_WITH_TZ_STR, APP_TIME_FORMAT_STR , ISO_TIME_FORMAT_STR };
    	for (String trialFormat : trialFormats ){
			try {
				return getFormat(trialFormat).parse(time);
			} catch (ParseException e) {
				//
			}
	}
    	throw new RuntimeException("Unknown Time Format");
    }

    public static String getTimeStr(final Date time) {
        return getTimeStr(time, TimeZone.getDefault());
    }
    public static String getTimeStr(final Date time,TimeZone zone) {
        return getTimestampStr(time, zone, APP_TIME_FORMAT_WITH_TZ_STR);
    }


    public static String getDateStr(final Date date) {
        return getTimestampStr(date, TimeZone.getDefault(),APP_DATE_FORMAT_STR);
    }

    public static String getTimestampStr(final Date date){
    	return getTimestampStr(date,TimeZone.getDefault(),APP_DATE_TIME_FORMAT_WITH_TZ_STR);
    }
    
    public static String getTimestampStr(final Date date,final String tz){
    	TimeZone zone = SimpleTimeZone.getTimeZone(tz);
    	return getTimestampStr(date,zone,APP_DATE_TIME_FORMAT_WITH_TZ_STR);
    }
    public static String getTimestampStr(final Date inOneTimeZone, final TimeZone zone, String datefmt){
    	DateFormat fmt = getFormat(datefmt);
    	fmt.setTimeZone(zone);
        return fmt.format(inOneTimeZone);
    }
    
    
    public static Date getDate(final Date inOneTimeZone, final String tz) {
        final TimeZone zone = SimpleTimeZone.getTimeZone(tz);

        final Calendar target = Calendar.getInstance(zone);
        final Calendar cal = Calendar.getInstance();

        cal.setTime(inOneTimeZone);

        final int fromOffset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
        final int toOffset = target.get(Calendar.ZONE_OFFSET) + target.get(Calendar.DST_OFFSET);

        final int diffOffset = toOffset - fromOffset;

        cal.add(Calendar.MILLISECOND, diffOffset);

        target.setTimeInMillis(cal.getTime().getTime());
        return target.getTime();
    }
    public static String getDOW(Date date) { 
		String[] DOW =new String[] {"SUN","MON", "TUE","WED","THU","FRI","SAT"}; 

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return DOW[c.get(Calendar.DAY_OF_WEEK)-1];
	}

    public static Date max(final Date... dates){
        Date max = null ;
        if (dates != null){
            for (Date date: dates){
                if (date != null) {
                    if (max == null || max.compareTo(date) < 0 ) {
                        max = date;
                    }
                }
            }
        }
        return max;
    }
    public static Date min(final Date... dates){
        Date min = null ;
        if (dates != null){
            for (Date date: dates){
                if (date != null) {
                    if (min == null || min.compareTo(date) > 0 ) {
                        min = date;
                    }
                }
            }
        }
        return min;
    }

}
