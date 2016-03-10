package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.SptMemberType;

public class SptMemberTypeFindCriteriaResp extends CommonCriteriaResp {
	private List<SptMemberType> memberTyps;

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

}
