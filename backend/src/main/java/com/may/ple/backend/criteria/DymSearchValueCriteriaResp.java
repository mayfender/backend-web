package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.DymSearchValue;

public class DymSearchValueCriteriaResp extends CommonCriteriaResp {
	private List<DymSearchValue> dymSearchValue;
	
	public DymSearchValueCriteriaResp() {}
	
	public DymSearchValueCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<DymSearchValue> getDymSearchValue() {
		return dymSearchValue;
	}

	public void setDymSearchValue(List<DymSearchValue> dymSearchValue) {
		this.dymSearchValue = dymSearchValue;
	}

}
