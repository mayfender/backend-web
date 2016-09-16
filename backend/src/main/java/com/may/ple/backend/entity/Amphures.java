package com.may.ple.backend.entity;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Amphures implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private String amphurCode;
	private String amphurName;
	private String amphurNameEng;
	private Long geoId;
	private Long provinceId;
	
	public Amphures(){}
	
	public Amphures(Long id, String amphurCode, String amphurName, String amphurNameEng, Long geoId, Long provinceId) {
		this.id = id;
		this.amphurCode = amphurCode;
		this.amphurName = amphurName;
		this.geoId = geoId;
		this.provinceId = provinceId;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAmphurCode() {
		return amphurCode;
	}

	public void setAmphurCode(String amphurCode) {
		this.amphurCode = amphurCode;
	}

	public String getAmphurName() {
		return amphurName;
	}

	public void setAmphurName(String amphurName) {
		this.amphurName = amphurName;
	}

	public String getAmphurNameEng() {
		return amphurNameEng;
	}

	public void setAmphurNameEng(String amphurNameEng) {
		this.amphurNameEng = amphurNameEng;
	}

	public Long getGeoId() {
		return geoId;
	}

	public void setGeoId(Long geoId) {
		this.geoId = geoId;
	}

	public Long getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Long provinceId) {
		this.provinceId = provinceId;
	}
	
}
