package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.DymSearch;

public class DymSearchCriteriaResp extends CommonCriteriaResp {
	private List<DymSearch> dymSearch;
	
	public DymSearchCriteriaResp() {}
	
	public DymSearchCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<DymSearch> getDymSearch() {
		return dymSearch;
	}

	public void setDymSearch(List<DymSearch> dymSearch) {
		this.dymSearch = dymSearch;
	}

}
