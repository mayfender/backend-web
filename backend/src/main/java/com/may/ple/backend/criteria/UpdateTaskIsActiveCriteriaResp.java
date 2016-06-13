package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class UpdateTaskIsActiveCriteriaResp extends CommonCriteriaResp {
	private Long noOwnerCount;
	
	public UpdateTaskIsActiveCriteriaResp() {}
	
	public UpdateTaskIsActiveCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getNoOwnerCount() {
		return noOwnerCount;
	}

	public void setNoOwnerCount(Long noOwnerCount) {
		this.noOwnerCount = noOwnerCount;
	}

}
