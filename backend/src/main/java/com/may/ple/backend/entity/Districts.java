package com.may.ple.backend.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class Districts implements Serializable {
	private static final long serialVersionUID = -567738520505915915L;
	@Id
	private Long districtId;
	private String districtCode;
	private String districtName;
	private String districtNameEng;
	
	protected Districts() {}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getDistrictId() {
		return districtId;
	}
	public void setDistrictId(Long districtId) {
		this.districtId = districtId;
	}
	public String getDistrictCode() {
		return districtCode;
	}
	public void setDistrictCode(String districtCode) {
		this.districtCode = districtCode;
	}
	public String getDistrictName() {
		return districtName;
	}
	public void setDistrictName(String districtName) {
		this.districtName = districtName;
	}
	public String getDistrictNameEng() {
		return districtNameEng;
	}
	public void setDistrictNameEng(String districtNameEng) {
		this.districtNameEng = districtNameEng;
	}

}
