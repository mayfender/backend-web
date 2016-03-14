package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.SptRegistration;

public class SptRegistrationSaveCriteriaResp extends CommonCriteriaResp {
	private List<SptRegistration> registereds;
	private Long totalItems;
	
	public SptRegistrationSaveCriteriaResp(){}
	
	public SptRegistrationSaveCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<SptRegistration> getRegistereds() {
		return registereds;
	}
	public void setRegistereds(List<SptRegistration> registereds) {
		this.registereds = registereds;
	}
	public Long getTotalItems() {
		return totalItems;
	}
	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

}
