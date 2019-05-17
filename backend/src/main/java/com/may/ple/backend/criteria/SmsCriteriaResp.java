package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SmsCriteriaResp extends CommonCriteriaResp {
	private List<Map> smses;
	private Long totalItems;
	
	public SmsCriteriaResp() {}
	
	public SmsCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

	public List<Map> getSmses() {
		return smses;
	}

	public void setSmses(List<Map> smses) {
		this.smses = smses;
	}

}
