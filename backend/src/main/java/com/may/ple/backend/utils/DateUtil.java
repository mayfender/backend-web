package com.may.ple.backend.utils;

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
	
	private static String ddMMChkDigit(String val) {
		return val.length() == 1 ? "0" + val : val;
	}
	
}