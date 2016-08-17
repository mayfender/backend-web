package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Users;

public class TraceResultCriteriaResp extends CommonCriteriaResp {
	private List<Map> traceDatas;
	private List<ColumnFormat> headers;
	private Long totalItems;
	private Double appointAmountTotal;
	private Long noOwnerCount;
	private String balanceColumn;
	private List<Users> users;
	private Map<String, Long> userTaskCount;
	
	public TraceResultCriteriaResp(){}
	
	public TraceResultCriteriaResp(int statusCode) {
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

	public Long getNoOwnerCount() {
		return noOwnerCount;
	}

	public void setNoOwnerCount(Long noOwnerCount) {
		this.noOwnerCount = noOwnerCount;
	}

	public String getBalanceColumn() {
		return balanceColumn;
	}

	public void setBalanceColumn(String balanceColumn) {
		this.balanceColumn = balanceColumn;
	}

	public List<Users> getUsers() {
		return users;
	}

	public void setUsers(List<Users> users) {
		this.users = users;
	}

	public Map<String, Long> getUserTaskCount() {
		return userTaskCount;
	}

	public void setUserTaskCount(Map<String, Long> userTaskCount) {
		this.userTaskCount = userTaskCount;
	}

	public List<Map> getTraceDatas() {
		return traceDatas;
	}

	public void setTraceDatas(List<Map> traceDatas) {
		this.traceDatas = traceDatas;
	}

	public Double getAppointAmountTotal() {
		return appointAmountTotal;
	}

	public void setAppointAmountTotal(Double appointAmountTotal) {
		this.appointAmountTotal = appointAmountTotal;
	}

}
