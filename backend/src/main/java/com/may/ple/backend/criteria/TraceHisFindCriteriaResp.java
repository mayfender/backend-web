package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TraceHisFindCriteriaResp extends CommonCriteriaResp {
	private List<Map> traceWorkHises;
	
	public TraceHisFindCriteriaResp(){}
	
	public TraceHisFindCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Map> getTraceWorkHises() {
		return traceWorkHises;
	}

	public void setTraceWorkHises(List<Map> traceWorkHises) {
		this.traceWorkHises = traceWorkHises;
	}

}
