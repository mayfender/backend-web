package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.SptMasterNamingDet;

public class ProfileUpdateCriteriaResp extends CommonCriteriaResp {
	private String userNameShow;
	private Long workPositionId;
	private List<SptMasterNamingDet> namingDetails;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getUserNameShow() {
		return userNameShow;
	}

	public void setUserNameShow(String userNameShow) {
		this.userNameShow = userNameShow;
	}

	public List<SptMasterNamingDet> getNamingDetails() {
		return namingDetails;
	}

	public void setNamingDetails(List<SptMasterNamingDet> namingDetails) {
		this.namingDetails = namingDetails;
	}

	public Long getWorkPositionId() {
		return workPositionId;
	}

	public void setWorkPositionId(Long workPositionId) {
		this.workPositionId = workPositionId;
	}

}
