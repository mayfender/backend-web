package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ProfileUpdateCriteriaResp extends CommonCriteriaResp {
	private byte[] defaultThumbnail;

	public ProfileUpdateCriteriaResp() {
	}

	public ProfileUpdateCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public byte[] getDefaultThumbnail() {
		return defaultThumbnail;
	}

	public void setDefaultThumbnail(byte[] defaultThumbnail) {
		this.defaultThumbnail = defaultThumbnail;
	}

}
