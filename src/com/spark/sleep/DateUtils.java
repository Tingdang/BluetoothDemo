package com.spark.sleep;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DateUtils {
	public static final String TAG = DateUtils.class.getSimpleName(); 
	public static final String BEGIN = "begin";
	public static final String END = "end";
	public static final long DAY_SPAN = 24 * 60 * 60 * 1000;
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());

	public static Date moveDay(Date date, int move) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(date);
		gregorianCalendar.add(Calendar.DATE, move);
		return gregorianCalendar.getTime();
	}
	
	public static int getIntervalDay_absolute(Date early, Date late) {
		if(early == null || late == null)
			return -1;
		
		long intervalMilli = late.getTime() - early.getTime();
		return (int)(intervalMilli / DAY_SPAN);
	}
	
	public static int getIntervalDay_relative(Date early, Date late) {
		if(early == null || late == null)
			return -1;
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(early);
		int day1 = calendar.get(Calendar.DAY_OF_YEAR);
		
		calendar.setTime(late);
		int day2 = calendar.get(Calendar.DAY_OF_YEAR);

		return day2 - day1; 
	}

	public static Map<String, Date> getDayStartEnd(Date date){
		Map<String,Date> map = new HashMap<String, Date>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00",Locale.getDefault());
			String str = sdf.format(date);
			Date date2 = simpleDateFormat.parse(str);
			map.put(BEGIN, date2);
			//Trace.e(TAG, "day start："+ simpleDateFormat.format(date2));

			Date date3 = new Date(date2.getTime() + DAY_SPAN - 1000);
			map.put(END, date3);
			//Trace.e(TAG, "day end："+ simpleDateFormat.format(date3));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	public static Map<String, Date> getWeekStartEnd(Date date){
		Map<String,Date> map = new HashMap<String, Date>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00",Locale.getDefault());
			String str = sdf.format(date);
			Date date2 = simpleDateFormat.parse(str);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date2);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			date2 = cal.getTime();
			map.put(BEGIN, date2);
			//Trace.e(TAG, "week　start："+ simpleDateFormat.format(date2));

			cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			Date date3 = new Date(cal.getTimeInMillis() + DAY_SPAN - 1000);
			map.put(END, date3);
			//Trace.e(TAG, "week　end："+ simpleDateFormat.format(date3));
		}catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	public static Map<String, Date> getMonthStartEnd(Date date){
		Map<String,Date> map = new HashMap<String, Date>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-01 00:00:00",Locale.getDefault());
			String str = sdf.format(date);
			Date date2 = simpleDateFormat.parse(str);
			map.put(BEGIN, date2);
			//Trace.e(TAG, "month　start："+simpleDateFormat.format(date2));

			Calendar cal = Calendar.getInstance();
			cal.setTime(date2);
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
			Date date3 = new Date(cal.getTimeInMillis() + DAY_SPAN - 1000);
			map.put(END, date3);
			//Trace.e(TAG, "month end："+simpleDateFormat.format(date3));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
}
