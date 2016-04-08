package com.may.ple.backend.utils;

import java.util.Date;

import org.apache.log4j.Logger;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class DateUtil {
	private static final Logger LOG = Logger.getLogger(DateUtil.class.getName());
	
	public static String dateDiff(Date start, Date end) {
		try {
			
			if(end.getTime() < start.getTime()) {
				return null;
			}
			
			Interval interval = new Interval(start.getTime(), end.getTime());
			Period period = interval.toPeriod().normalizedStandard(PeriodType.yearMonthDay());
			
			/*PeriodFormatter formatter = new PeriodFormatterBuilder()
		            .appendYears()
		            .appendSuffix(" year ", " years ")
		            .appendSeparator("and ")
		            .appendMonths()
		            .appendSuffix(" month ", " months ")
		            .appendSeparator("and ")
		            .appendDays()
		            .appendSuffix(" day ", " days ")
		            .toFormatter();*/
			
			PeriodFormatter formatter = new PeriodFormatterBuilder()
		            .appendYears()
		            .appendSuffix(" ปี ")
		            .appendMonths()
		            .appendSuffix(" เดือน ")
		            .appendDays()
		            .appendSuffix(" วัน")
		            .toFormatter();
			
			return formatter.print(period);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
