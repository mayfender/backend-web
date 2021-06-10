package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DymListFindCriteriaReq {
	private String dymListId;
	private String productId;
	private List<Integer> statuses;
	private Boolean isLog;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public List<Integer> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<Integer> statuses) {
		this.statuses = statuses;
	}

	public String getDymListId() {
		return dymListId;
	}

	public void setDymListId(String dymListId) {
		this.dymListId = dymListId;
	}

	public Boolean getIsLog() {
		return isLog;
	}

	public void setIsLog(Boolean isLog) {
		this.isLog = isLog;
	}

}
