package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Users;

public class TaskDetailCriteriaResp extends CommonCriteriaResp {
	private List<Map> taskDetails;
	private List<ColumnFormat> headers;
	private List<ColumnFormat> headersPayment;
	private Long totalItems;
	private Long noOwnerCount;
	private String balanceColumn;
	private List<Users> users;
	private Map<String, Long> userTaskCount;
	
	public TaskDetailCriteriaResp(){}
	
	public TaskDetailCriteriaResp(int statusCode) {
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

	public List<Map> getTaskDetails() {
		return taskDetails;
	}

	public void setTaskDetails(List<Map> taskDetails) {
		this.taskDetails = taskDetails;
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

	public List<ColumnFormat> getHeadersPayment() {
		return headersPayment;
	}

	public void setHeadersPayment(List<ColumnFormat> headersPayment) {
		this.headersPayment = headersPayment;
	}

}
