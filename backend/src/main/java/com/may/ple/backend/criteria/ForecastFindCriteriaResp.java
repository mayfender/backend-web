package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Forecast;

public class ForecastFindCriteriaResp extends CommonCriteriaResp {
	private List<Forecast> forecastList;
	private Long totalItems;
	
	public ForecastFindCriteriaResp() {}
	
	public ForecastFindCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Forecast> getForecastList() {
		return forecastList;
	}

	public void setForecastList(List<Forecast> forecastList) {
		this.forecastList = forecastList;
	}

	public Long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

}
