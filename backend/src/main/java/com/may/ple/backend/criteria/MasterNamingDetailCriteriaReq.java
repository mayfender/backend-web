package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class MasterNamingDetailCriteriaReq {
	private Long masterNamingDetailId;
	private Long masterNamingId;
	private String displayValue;
	private Integer status;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getMasterNamingId() {
		return masterNamingId;
	}
	public void setMasterNamingId(Long masterNamingId) {
		this.masterNamingId = masterNamingId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getDisplayValue() {
		return displayValue;
	}
	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}
	public Long getMasterNamingDetailId() {
		return masterNamingDetailId;
	}
	public void setMasterNamingDetailId(Long masterNamingDetailId) {
		this.masterNamingDetailId = masterNamingDetailId;
	}

}
