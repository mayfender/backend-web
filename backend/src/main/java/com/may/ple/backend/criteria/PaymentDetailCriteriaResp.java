package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Users;

public class PaymentDetailCriteriaResp extends CommonCriteriaResp {
	private List<Map> paymentDetails;
	private List<ColumnFormat> headers;
	private List<ColumnFormat> taskDetailHeaders;
	private Long totalItems;
	private List<Users> users;
	
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

	public List<Users> getUsers() {
		return users;
	}

	public void setUsers(List<Users> users) {
		this.users = users;
	}

	public List<ColumnFormat> getTaskDetailHeaders() {
		return taskDetailHeaders;
	}

	public void setTaskDetailHeaders(List<ColumnFormat> taskDetailHeaders) {
		this.taskDetailHeaders = taskDetailHeaders;
	}

}
