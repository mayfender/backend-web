package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ProfileUpdateCriteriaResp extends CommonCriteriaResp {
	private String userNameShow;

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

}
