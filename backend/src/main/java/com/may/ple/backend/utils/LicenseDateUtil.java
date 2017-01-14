package com.may.ple.backend.utils;

import java.util.Date;

import org.apache.log4j.Logger;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class LicenseDateUtil {
	private static final Logger LOG = Logger.getLogger(LicenseDateUtil.class.getName());
	
	public static LicenseResultUtil licenseDate(Date start, Date end) {
		LicenseResultUtil result = new LicenseResultUtil();
		
		try {
			
			if(end.getTime() < start.getTime()) {
				return null;
			}
			
			Interval interval = new Interval(start.getTime(), end.getTime());
			Period period = interval.toPeriod().normalizedStandard(PeriodType.yearMonthDayTime());
						
			PeriodFormatterBuilder formatBuilder = new PeriodFormatterBuilder()
			.appendYears()
			.appendSuffix(" year ", " years ")
			.appendSeparator("and ")
			.appendMonths()
			.appendSuffix(" month ", " months ")
			.appendSeparator("and ")
			.appendDays()
			.appendSuffix(" day ", " days ");
					
			if(period.getDays() == 0) {
				formatBuilder.appendHours()
				.appendSuffix(" hour ", " hours ");	
			}
			
			PeriodFormatter formatter = formatBuilder.toFormatter();
			
			
			
			
			
			/*PeriodFormatter formatter = new PeriodFormatterBuilder()
		            .appendYears()
		            .appendSuffix(" ปี ")
		            .appendMonths()
		            .appendSuffix(" เดือน ")
		            .appendDays()
		            .appendSuffix(" วัน")
		            .toFormatter();*/
			
			
			result.setMessage(formatter.print(period));
			result.setDays(period.getDays());
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
