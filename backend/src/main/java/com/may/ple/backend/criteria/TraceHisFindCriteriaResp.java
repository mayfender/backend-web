package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.TraceWorkUpdatedHistory;

public class TraceHisFindCriteriaResp extends CommonCriteriaResp {
	private List<TraceWorkUpdatedHistory> traceWorkHises;
	
	public TraceHisFindCriteriaResp(){}
	
	public TraceHisFindCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<TraceWorkUpdatedHistory> getTraceWorkHises() {
		return traceWorkHises;
	}

	public void setTraceWorkHises(List<TraceWorkUpdatedHistory> traceWorkHises) {
		this.traceWorkHises = traceWorkHises;
	}

}
