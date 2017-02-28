package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.DymList;

public class DymListFindCriteriaResp extends CommonCriteriaResp {
	private List<DymList> dymList;
	
	public DymListFindCriteriaResp() {}
	
	public DymListFindCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<DymList> getDymList() {
		return dymList;
	}

	public void setDymList(List<DymList> dymList) {
		this.dymList = dymList;
	}

}
