package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class Zipcodes implements Serializable {
	private static final long serialVersionUID = -567738520505915915L;
	@Id
	private Long id;
	private String zipCode;
	private String districtCode;
	@OneToMany
	@JoinColumn(name="districtCode", referencedColumnName="districtCode")
	private List<Districts> districts;
	
	protected Zipcodes() {}
	
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
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public List<Districts> getDistricts() {
		return districts;
	}
	public void setDistricts(List<Districts> districts) {
		this.districts = districts;
	}
	public String getDistrictCode() {
		return districtCode;
	}
	public void setDistrictCode(String districtCode) {
		this.districtCode = districtCode;
	}

}
