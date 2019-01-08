package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DashboardCollectorWorkCriteriaResp extends CommonCriteriaResp {
	private List<Map> collectorWork;
	private String balanceColumnName;
	
	public DashboardCollectorWorkCriteriaResp() {}
	
	public DashboardCollectorWorkCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Map> getCollectorWork() {
		return collectorWork;
	}

	public void setCollectorWork(List<Map> collectorWork) {
		this.collectorWork = collectorWork;
	}

	public String getBalanceColumnName() {
		return balanceColumnName;
	}

	public void setBalanceColumnName(String balanceColumnName) {
		this.balanceColumnName = balanceColumnName;
	}

}
