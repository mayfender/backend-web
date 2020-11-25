package com.may.ple.backend.utils;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Seconds;

import com.may.ple.backend.constant.RolesConstant;
import com.may.ple.backend.entity.ProductSetting;

public class WorkingTimeUtil {
	private static final Logger LOG = Logger.getLogger(WorkingTimeUtil.class.getName());

	public static Integer workingTimeCalculation(ProductSetting setting, RolesConstant rolesConstant) {
	    if(rolesConstant != RolesConstant.ROLE_USER && rolesConstant != RolesConstant.ROLE_SUPERVISOR) {
	    	return null;
	    }

		LocalTime startTime, endTime;
		LocalDate newDate = new LocalDate();
		LocalTime nowTime = new LocalTime();
		int dayOfWeek = newDate.getDayOfWeek();
		Integer startTimeH, startTimeM, endTimeH, endTimeM;
		Boolean isEnable;
		int seconds;

		if(DateTimeConstants.SATURDAY == dayOfWeek) {
			isEnable = setting.getSatWorkingDayEnable();
			startTimeH = setting.getSatStartTimeH();
			startTimeM = setting.getSatStartTimeM();
			endTimeH = setting.getSatEndTimeH();
			endTimeM = setting.getSatEndTimeM();
		} else if(DateTimeConstants.SUNDAY == dayOfWeek) {
			isEnable = setting.getSunWorkingDayEnable();
			startTimeH = setting.getSunStartTimeH();
			startTimeM = setting.getSunStartTimeM();
			endTimeH = setting.getSunEndTimeH();
			endTimeM = setting.getSunEndTimeM();
		} else {
			isEnable = setting.getNormalWorkingDayEnable();
			startTimeH = setting.getNormalStartTimeH();
			startTimeM = setting.getNormalStartTimeM();
			endTimeH = setting.getNormalEndTimeH();
			endTimeM = setting.getNormalEndTimeM();
		}

		if(isEnable != null && !isEnable) {
			LOG.info("Working time is disabled");
			return null;
		}

		if(startTimeH != null && startTimeM != null) {
			startTime = new LocalTime(startTimeH, startTimeM);
			seconds = Seconds.secondsBetween(startTime, nowTime).getSeconds();

			if(seconds <= 0) return seconds;
		}

		if(endTimeH != null && endTimeM != null) {
			endTime = new LocalTime(endTimeH, endTimeM);
			seconds = Seconds.secondsBetween(nowTime, endTime).getSeconds();

			if(seconds < 0) seconds = 0;

			return seconds;
		}

		return null;
	}

}
