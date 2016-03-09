package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class MasterNamingDetailSaveCriteriaResp extends CommonCriteriaResp {
	private Long namingDetId;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getNamingDetId() {
		return namingDetId;
	}
	public void setNamingDetId(Long namingDetId) {
		this.namingDetId = namingDetId;
	}

}
