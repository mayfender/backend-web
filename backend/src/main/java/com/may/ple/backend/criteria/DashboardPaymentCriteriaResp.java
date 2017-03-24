package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DashboardPaymentCriteriaResp extends CommonCriteriaResp {
	private Map<String, List<Double>> datas;
	private List<String> labels;
	
	public DashboardPaymentCriteriaResp() {}
	
	public DashboardPaymentCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Map<String, List<Double>> getDatas() {
		return datas;
	}

	public void setDatas(Map<String, List<Double>> datas) {
		this.datas = datas;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

}
