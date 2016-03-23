package com.may.ple.backend.criteria;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.SptMemberType;
import com.may.ple.backend.entity.SptRegistration;

public class SptRegistrationEditCriteriaResp extends CommonCriteriaResp {
	private List<SptMemberType> memberTyps;
	private SptRegistration registration;
	private Date todayDate;
	
	public SptRegistrationEditCriteriaResp(){}
	
	public SptRegistrationEditCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<SptMemberType> getMemberTyps() {
		return memberTyps;
	}
	public void setMemberTyps(List<SptMemberType> memberTyps) {
		this.memberTyps = memberTyps;
	}
	public SptRegistration getRegistration() {
		return registration;
	}
	public void setRegistration(SptRegistration registration) {
		this.registration = registration;
	}
	public Date getTodayDate() {
		return todayDate;
	}
	public void setTodayDate(Date todayDate) {
		this.todayDate = todayDate;
	}

}
