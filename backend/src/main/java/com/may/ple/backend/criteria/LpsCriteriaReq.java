package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class LpsCriteriaReq {
	private String lpsGroup;
	private String lpsNumber;
	private String latitude;
	private String longitude;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getLpsGroup() {
		return lpsGroup;
	}

	public void setLpsGroup(String lpsGroup) {
		this.lpsGroup = lpsGroup;
	}

	public String getLpsNumber() {
		return lpsNumber;
	}

	public void setLpsNumber(String lpsNumber) {
		this.lpsNumber = lpsNumber;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

}
