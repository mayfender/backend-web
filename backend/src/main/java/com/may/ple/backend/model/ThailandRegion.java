package com.may.ple.backend.model;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Amphures;
import com.may.ple.backend.entity.Districts;
import com.may.ple.backend.entity.Provinces;
import com.may.ple.backend.entity.Zipcodes;

public class ThailandRegion implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<Amphures> amphures;
	private List<Districts> districts;
	private List<Provinces> provinces;
	private List<Zipcodes> zipcodes;
	
	public ThailandRegion(){}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Districts> getDistricts() {
		return districts;
	}

	public void setDistricts(List<Districts> districts) {
		this.districts = districts;
	}

	public List<Provinces> getProvinces() {
		return provinces;
	}

	public void setProvinces(List<Provinces> provinces) {
		this.provinces = provinces;
	}

	public List<Zipcodes> getZipcodes() {
		return zipcodes;
	}

	public void setZipcodes(List<Zipcodes> zipcodes) {
		this.zipcodes = zipcodes;
	}

	public List<Amphures> getAmphures() {
		return amphures;
	}

	public void setAmphures(List<Amphures> amphures) {
		this.amphures = amphures;
	}

}
