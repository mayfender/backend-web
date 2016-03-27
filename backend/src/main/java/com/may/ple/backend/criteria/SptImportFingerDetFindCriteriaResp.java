package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.SptImportFingerDet;

public class SptImportFingerDetFindCriteriaResp extends CommonCriteriaResp {
	private List<SptImportFingerDet> fingerDet;
	private Long totalItems;
	
	public SptImportFingerDetFindCriteriaResp(){}
	
	public SptImportFingerDetFindCriteriaResp(int statusCode) {
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
	public List<SptImportFingerDet> getFingerDet() {
		return fingerDet;
	}
	public void setFingerDet(List<SptImportFingerDet> fingerDet) {
		this.fingerDet = fingerDet;
	}

}
