package com.may.ple.backend.criteria;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.SptMemberType;

public class RenewalPrepareDataCriteriaResp extends CommonCriteriaResp {
	private List<SptMemberType> memberTypes;
	private Date todayDate;
	
	public RenewalPrepareDataCriteriaResp(){}
	
	public RenewalPrepareDataCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Date getTodayDate() {
		return todayDate;
	}
	public void setTodayDate(Date todayDate) {
		this.todayDate = todayDate;
	}
	public List<SptMemberType> getMemberTypes() {
		return memberTypes;
	}
	public void setMemberTypes(List<SptMemberType> memberTypes) {
		this.memberTypes = memberTypes;
	}

}
