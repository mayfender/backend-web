package com.may.ple.backend.utils;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ibm.icu.util.Calendar;

public class YearUtil {
	private static final Logger LOG = Logger.getLogger(YearUtil.class.getName());

	/**
	 * Convert Buddhist Year to Gregorian Year.
	 * @param date
	 * @throws Exception
	 */
	public static Date buddToGre(Date date) throws Exception {
		try {
			boolean isBudd = isBudd(date);
			if(!isBudd) return date;

			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int convertedYear = cal.get(Calendar.YEAR) - 543;
			cal.set(Calendar.YEAR, convertedYear);
			return cal.getTime();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	/**
	 * Check Year
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static boolean isBudd(Date date) throws ParseException {
		Calendar cal = Calendar.getInstance();
		int yearThreshold = cal.get(Calendar.YEAR) + 272; //-----: (543 / 2) = 272
		cal.setTime(date);
		boolean isBudd = cal.get(Calendar.YEAR) > yearThreshold;
		return isBudd;
	}

}
