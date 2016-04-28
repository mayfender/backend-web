package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.dto.ReportMoneyDto;

public class ReportMoneyCriteriaResp extends CommonCriteriaResp {
	private List<ReportMoneyDto> moneys;
	
	public ReportMoneyCriteriaResp() {}
	
	public ReportMoneyCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<ReportMoneyDto> getMoneys() {
		return moneys;
	}
	public void setMoneys(List<ReportMoneyDto> moneys) {
		this.moneys = moneys;
	}

}
