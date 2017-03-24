package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DashboardPaymentCriteriaResp extends CommonCriteriaResp {
	private List<Map> payment;
	
	public DashboardPaymentCriteriaResp() {}
	
	public DashboardPaymentCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Map> getPayment() {
		return payment;
	}

	public void setPayment(List<Map> payment) {
		this.payment = payment;
	}

}
