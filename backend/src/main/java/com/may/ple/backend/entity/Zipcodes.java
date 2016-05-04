package com.may.ple.backend.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class Zipcodes implements Serializable {
	private static final long serialVersionUID = -567738520505915915L;
	@Id
	private Long id;
	private String zipcode;
	@OneToOne
	@JoinColumn(name="districtCode", referencedColumnName="districtCode")
	private Districts district;
	
	protected Zipcodes() {}
	
	public Zipcodes(String zipcode, Districts district) {
		this.zipcode = zipcode;
		this.district = district;
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
	public String getZipcode() {
		return zipcode;
	}
	public void setZipCode(String zipcode) {
		this.zipcode = zipcode;
	}
	public Districts getDistrict() {
		return district;
	}
	public void setDistrict(Districts district) {
		this.district = district;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

}
