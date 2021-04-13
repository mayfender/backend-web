package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class UploadFileCriteriaResp extends CommonCriteriaResp {
	private String id;
	private Map lastPeriod;
	private List<Map> orderFiles;
	private long totalItems;
	private int errCode;
	private List<String> customerNameLst;

	public UploadFileCriteriaResp() {}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map getLastPeriod() {
		return lastPeriod;
	}

	public void setLastPeriod(Map lastPeriod) {
		this.lastPeriod = lastPeriod;
	}

	public long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(long totalItems) {
		this.totalItems = totalItems;
	}

	public List<Map> getOrderFiles() {
		return orderFiles;
	}

	public void setOrderFiles(List<Map> orderFiles) {
		this.orderFiles = orderFiles;
	}

	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public List<String> getCustomerNameLst() {
		return customerNameLst;
	}

	public void setCustomerNameLst(List<String> customerNameLst) {
		this.customerNameLst = customerNameLst;
	}

}
