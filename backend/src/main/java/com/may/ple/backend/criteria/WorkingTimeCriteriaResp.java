package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class WorkingTimeCriteriaResp extends CommonCriteriaResp {
	private Integer startTimeH;
	private Integer startTimeM;
	private Integer endTimeH;
	private Integer endTimeM;
	
	public WorkingTimeCriteriaResp(){}
	
	public WorkingTimeCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Integer getStartTimeH() {
		return startTimeH;
	}

	public void setStartTimeH(Integer startTimeH) {
		this.startTimeH = startTimeH;
	}

	public Integer getStartTimeM() {
		return startTimeM;
	}

	public void setStartTimeM(Integer startTimeM) {
		this.startTimeM = startTimeM;
	}

	public Integer getEndTimeH() {
		return endTimeH;
	}

	public void setEndTimeH(Integer endTimeH) {
		this.endTimeH = endTimeH;
	}

	public Integer getEndTimeM() {
		return endTimeM;
	}

	public void setEndTimeM(Integer endTimeM) {
		this.endTimeM = endTimeM;
	}

}
