package com.may.ple.backend.entity;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Provinces implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private String provinceCode;
	private String provinceName;
	private String provinceNameEng;
	private Long geoId;
		
	public Provinces(){}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Provinces(Long id, String provinceCode, String provinceName, String provinceNameEng, Long geoId) {
		this.id = id;
		this.provinceCode = provinceCode;
		this.provinceName = provinceName;
		this.provinceNameEng = provinceNameEng;
		this.geoId = geoId;
	}

	public String getProvinceCode() {
		return provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getProvinceNameEng() {
		return provinceNameEng;
	}

	public void setProvinceNameEng(String provinceNameEng) {
		this.provinceNameEng = provinceNameEng;
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
