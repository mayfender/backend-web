package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Dealer;

public class DealerCriteriaResp extends CommonCriteriaResp {
	private List<Dealer> dealers;

	public DealerCriteriaResp() {}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Dealer> getDealers() {
		return dealers;
	}

	public void setDealers(List<Dealer> dealers) {
		this.dealers = dealers;
	}

}
