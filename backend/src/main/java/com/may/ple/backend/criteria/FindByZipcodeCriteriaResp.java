package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Zipcodes;

public class FindByZipcodeCriteriaResp extends CommonCriteriaResp {
	private List<Zipcodes> zipcode;
	
	public FindByZipcodeCriteriaResp(){}
	
	public FindByZipcodeCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Zipcodes> getZipcode() {
		return zipcode;
	}
	public void setZipcode(List<Zipcodes> zipcode) {
		this.zipcode = zipcode;
	}

}
