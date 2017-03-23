package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DashboardTraceCountCriteriaResp extends CommonCriteriaResp {
	private List<Map> traceCount;
	
	public DashboardTraceCountCriteriaResp() {}
	
	public DashboardTraceCountCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Map> getTraceCount() {
		return traceCount;
	}

	public void setTraceCount(List<Map> traceCount) {
		this.traceCount = traceCount;
	}

}
