package com.may.ple.backend.utils;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import com.may.ple.backend.constant.CompareDateStatusConstant;

public class TaskDetailStatusUtil {
	
	public static int getStatus(Date comparedAppointDate, Date comparedNextTimeDate) {
		CompareDateStatusConstant status = CompareDateStatusConstant.NORMAL;
		Date date = Calendar.getInstance().getTime();
		
		if(comparedAppointDate != null) {
			if(DateUtils.isSameDay(date, comparedAppointDate)) {
				status = CompareDateStatusConstant.TODAY_APPOINT_DATE;
			} else if(date.after(comparedAppointDate)) {
				status = CompareDateStatusConstant.OVER_DATE;
			} else if(comparedNextTimeDate != null){
				if(DateUtils.isSameDay(date, comparedNextTimeDate)) {
					status = CompareDateStatusConstant.TODAY_NEXT_TIME_DATE;
				} else if(date.after(comparedNextTimeDate)) {
					status = CompareDateStatusConstant.OVER_DATE;
				}
			}
		} else if(comparedNextTimeDate != null){
			if(DateUtils.isSameDay(date, comparedNextTimeDate)) {
				status = CompareDateStatusConstant.TODAY_NEXT_TIME_DATE;
			} else if(date.after(comparedNextTimeDate)) {
				status = CompareDateStatusConstant.OVER_DATE;
			}
		}
		
		return status.getStatus();
	}

}
