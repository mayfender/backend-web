package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.DymListDet;
import com.may.ple.backend.entity.DymListDetGroup;

public class DymListDetFindCriteriaResp extends CommonCriteriaResp {
	private List<DymListDet> dymListDet;
	private List<DymListDetGroup> dymListDetGroup;
	
	public DymListDetFindCriteriaResp() {}
	
	public DymListDetFindCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<DymListDet> getDymListDet() {
		return dymListDet;
	}

	public void setDymListDet(List<DymListDet> dymListDet) {
		this.dymListDet = dymListDet;
	}

	public List<DymListDetGroup> getDymListDetGroup() {
		return dymListDetGroup;
	}

	public void setDymListDetGroup(List<DymListDetGroup> dymListDetGroup) {
		this.dymListDetGroup = dymListDetGroup;
	}
	
}
