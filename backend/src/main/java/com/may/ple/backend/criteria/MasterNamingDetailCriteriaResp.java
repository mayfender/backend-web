package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.MasterNamingDetail;

public class MasterNamingDetailCriteriaResp extends CommonCriteriaResp {
	private List<MasterNamingDetail> namingDetails;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<MasterNamingDetail> getNamingDetails() {
		return namingDetails;
	}
	public void setNamingDetails(List<MasterNamingDetail> namingDetails) {
		this.namingDetails = namingDetails;
	}

}
