package com.may.ple.backend.entity;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Districts implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private String districtCode;
	private String districtName;
	private String districtNameEng;
	private Long amphurId;
	private Long provinceId;
	private Long geoId;
		
	public Districts(){}

	public Districts(Long id, String districtCode, String districtName, String districtNameEng, Long amphurId, Long provinceId, Long geoId) {
		this.id = id;
		this.districtCode = districtCode;
		this.districtName = districtName;
		this.districtNameEng = districtNameEng;
		this.amphurId = amphurId;
		this.provinceId = provinceId;
		this.geoId = geoId;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
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

	public Long getAmphurId() {
		return amphurId;
	}

	public void setAmphurId(Long amphurId) {
		this.amphurId = amphurId;
	}

	public Long getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Long provinceId) {
		this.provinceId = provinceId;
	}

	public Long getGeoId() {
		return geoId;
	}

	public void setGeoId(Long geoId) {
		this.geoId = geoId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}
