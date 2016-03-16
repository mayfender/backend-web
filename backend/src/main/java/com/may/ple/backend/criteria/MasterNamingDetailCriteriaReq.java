package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class MasterNamingDetailCriteriaReq {
	private Long masterNamingId;
	
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

}
