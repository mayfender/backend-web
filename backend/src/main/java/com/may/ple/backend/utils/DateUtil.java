package com.may.ple.backend.utils;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class DateUtil {
	private static final Logger LOG = Logger.getLogger(DateUtil.class.getName());
	
	public static String ddMMYYYYFormat(String date, boolean isMinus) throws Exception {
		try {
			String[] dates = date.split("/");
			String day = ddMMChkDigit(dates[0]);
			String month = ddMMChkDigit(dates[1]);
			String year = isMinus ? String.valueOf((Integer.parseInt(dates[2]) - 543)) : dates[2];
			return day + "/" + month + "/" + year;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public static Date getStartDate(Date date) {
		Calendar from = Calendar.getInstance();
		from.setTime(date);
		from.set(Calendar.HOUR_OF_DAY, 0);
		from.set(Calendar.MINUTE, 0);
		from.set(Calendar.SECOND, 0);
		from.set(Calendar.MILLISECOND, 0);
		return from.getTime();
	}
	
	public static Date getEndDate(Date date) {
		Calendar to = Calendar.getInstance();
		to.setTime(date);
		to.set(Calendar.HOUR_OF_DAY, 23);
		to.set(Calendar.MINUTE, 59);
		to.set(Calendar.SECOND, 59);
		to.set(Calendar.MILLISECOND, 999);
		return to.getTime();
	}
	
	private static String ddMMChkDigit(String val) {
		return val.length() == 1 ? "0" + val : val;
	}
	
}