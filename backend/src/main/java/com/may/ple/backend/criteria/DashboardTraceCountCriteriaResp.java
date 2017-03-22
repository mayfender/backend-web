package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ActionCode;

public class DashboardTraceCountCriteriaResp extends CommonCriteriaResp {
	private List<ActionCode> actionCodes;
	
	public DashboardTraceCountCriteriaResp() {}
	
	public DashboardTraceCountCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<ActionCode> getActionCodes() {
		return actionCodes;
	}

	public void setActionCodes(List<ActionCode> actionCodes) {
		this.actionCodes = actionCodes;
	}

}
