package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;

public class PaymentDetailCriteriaResp extends CommonCriteriaResp {
	private List<Map> paymentDetails;
	private List<ColumnFormat> headers;
	private Long totalItems;
	
	public PaymentDetailCriteriaResp(){}
	
	public PaymentDetailCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getTotalItems() {
		return totalItems;
	}
	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

	public List<ColumnFormat> getHeaders() {
		return headers;
	}

	public void setHeaders(List<ColumnFormat> headers) {
		this.headers = headers;
	}

	public List<Map> getPaymentDetails() {
		return paymentDetails;
	}

	public void setPaymentDetails(List<Map> paymentDetails) {
		this.paymentDetails = paymentDetails;
	}

}