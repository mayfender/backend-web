package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ForecastFindCriteriaResp extends CommonCriteriaResp {
	private List<Map> forecastList;
	private Long totalItems;
	
	public ForecastFindCriteriaResp() {}
	
	public ForecastFindCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Map> getForecastList() {
		return forecastList;
	}

	public void setForecastList(List<Map> forecastList) {
		this.forecastList = forecastList;
	}

	public Long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

}
