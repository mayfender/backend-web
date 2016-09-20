package com.may.ple.backend.entity;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Zipcodes implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private String districtCode;
	private String zipcode;
	
	public Zipcodes(){}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Zipcodes(Long id, String districtCode, String zipcode) {
		this.id = id;
		this.districtCode = districtCode;
		this.zipcode = zipcode;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDistrictCode() {
		return districtCode;
	}

	public void setDistrictCode(String districtCode) {
		this.districtCode = districtCode;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	
}
