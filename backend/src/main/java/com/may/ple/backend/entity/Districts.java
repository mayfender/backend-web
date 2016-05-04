package com.may.ple.backend.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class Districts implements Serializable {
	private static final long serialVersionUID = -567738520505915915L;
	@Id
	private Long districtId;
	private String districtCode;
	private String districtName;
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="amphur_id")
	private Amphures amphures;
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="province_id")
	private Provinces provinces;
	
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
	public String getDistrictName() {
		return districtName;
	}
	public void setDistrictName(String districtName) {
		this.districtName = districtName;
	}
	public Amphures getAmphures() {
		return amphures;
	}
	public void setAmphures(Amphures amphures) {
		this.amphures = amphures;
	}
	public Provinces getProvinces() {
		return provinces;
	}
	public void setProvinces(Provinces provinces) {
		this.provinces = provinces;
	}
	public String getDistrictCode() {
		return districtCode;
	}
	public void setDistrictCode(String districtCode) {
		this.districtCode = districtCode;
	}

}
