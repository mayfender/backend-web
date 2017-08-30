package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Users;

public class FindToPrintCriteriaResp extends CommonCriteriaResp {
	private List<ColumnFormat> headers;
	private Long totalItems;
	private List<Map> noticeToPrints;
	private List<Users> users;
	private String FileName;
	private Boolean isDisableNoticePrint;
	
	public FindToPrintCriteriaResp(){}
	
	public FindToPrintCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<ColumnFormat> getHeaders() {
		return headers;
	}

	public void setHeaders(List<ColumnFormat> headers) {
		this.headers = headers;
	}

	public Long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

	public List<Map> getNoticeToPrints() {
		return noticeToPrints;
	}

	public void setNoticeToPrints(List<Map> noticeToPrints) {
		this.noticeToPrints = noticeToPrints;
	}

	public List<Users> getUsers() {
		return users;
	}

	public void setUsers(List<Users> users) {
		this.users = users;
	}

	public String getFileName() {
		return FileName;
	}

	public void setFileName(String fileName) {
		FileName = fileName;
	}

	public Boolean getIsDisableNoticePrint() {
		return isDisableNoticePrint;
	}

	public void setIsDisableNoticePrint(Boolean isDisableNoticePrint) {
		this.isDisableNoticePrint = isDisableNoticePrint;
	}

}
