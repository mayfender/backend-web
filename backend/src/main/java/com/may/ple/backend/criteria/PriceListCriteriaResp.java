package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.PriceList;

public class PriceListCriteriaResp extends CommonCriteriaResp {
	private List<PriceList> priceList;

	public PriceListCriteriaResp() {}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public PriceListCriteriaResp(int statusCode) {
		super(statusCode);
	}

	public List<PriceList> getPriceList() {
		return priceList;
	}

	public void setPriceList(List<PriceList> priceList) {
		this.priceList = priceList;
	}

}
