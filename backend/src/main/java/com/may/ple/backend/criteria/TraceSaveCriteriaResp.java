package com.may.ple.backend.criteria;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TraceSaveCriteriaResp extends CommonCriteriaResp {
	private String id;
	private Integer traceStatus;
	private Date traceDate;
	
	public TraceSaveCriteriaResp(){}
	
	public TraceSaveCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getTraceStatus() {
		return traceStatus;
	}

	public void setTraceStatus(Integer traceStatus) {
		this.traceStatus = traceStatus;
	}

	public Date getTraceDate() {
		return traceDate;
	}

	public void setTraceDate(Date traceDate) {
		this.traceDate = traceDate;
	}

}
