package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SptMemberTypeSaveCriteriaReq {
	private String memberTypeName;
	private Integer durationType;
	private Integer durationQty;
	private Double memberPrice;
	private Integer status;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	
	public String getMemberTypeName() {
		return memberTypeName;
	}
	public void setMemberTypeName(String memberTypeName) {
		this.memberTypeName = memberTypeName;
	}
	public Integer getDurationType() {
		return durationType;
	}
	public void setDurationType(Integer durationType) {
		this.durationType = durationType;
	}
	public Integer getDurationQty() {
		return durationQty;
	}
	public void setDurationQty(Integer durationQty) {
		this.durationQty = durationQty;
	}
	public Double getMemberPrice() {
		return memberPrice;
	}
	public void setMemberPrice(Double memberPrice) {
		this.memberPrice = memberPrice;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
}
