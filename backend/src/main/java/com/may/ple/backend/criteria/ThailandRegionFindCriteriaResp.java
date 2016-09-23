package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Amphures;
import com.may.ple.backend.entity.Districts;
import com.may.ple.backend.entity.Provinces;

public class ThailandRegionFindCriteriaResp extends CommonCriteriaResp {
	private List<Provinces> provinces;
	private List<Amphures> amphures;
	private List<Districts> districts;
	private String zipcode;
	
	public ThailandRegionFindCriteriaResp(){}
	
	public ThailandRegionFindCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Provinces> getProvinces() {
		return provinces;
	}

	public void setProvinces(List<Provinces> provinces) {
		this.provinces = provinces;
	}

	public List<Amphures> getAmphures() {
		return amphures;
	}

	public void setAmphures(List<Amphures> amphures) {
		this.amphures = amphures;
	}

	public List<Districts> getDistricts() {
		return districts;
	}

	public void setDistricts(List<Districts> districts) {
		this.districts = districts;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

}
