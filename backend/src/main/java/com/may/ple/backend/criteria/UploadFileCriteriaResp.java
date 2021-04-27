package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class UploadFileCriteriaResp extends CommonCriteriaResp {
	private String id;
	private Map lastPeriod;
	private List<Map> orderFiles;
	private Map orderFile;
	private Long totalItems;
	private Integer errCode;
	private List<String> customerNameLst;
	private String base64Data;
	private Integer status;
	private Map<String, Integer> orderFileSum;

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

	public List<Map> getOrderFiles() {
		return orderFiles;
	}

	public void setOrderFiles(List<Map> orderFiles) {
		this.orderFiles = orderFiles;
	}

	public Map getOrderFile() {
		return orderFile;
	}

	public void setOrderFile(Map orderFile) {
		this.orderFile = orderFile;
	}

	public Long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

	public Integer getErrCode() {
		return errCode;
	}

	public void setErrCode(Integer errCode) {
		this.errCode = errCode;
	}

	public List<String> getCustomerNameLst() {
		return customerNameLst;
	}

	public void setCustomerNameLst(List<String> customerNameLst) {
		this.customerNameLst = customerNameLst;
	}

	public String getBase64Data() {
		return base64Data;
	}

	public void setBase64Data(String base64Data) {
		this.base64Data = base64Data;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Map<String, Integer> getOrderFileSum() {
		return orderFileSum;
	}

	public void setOrderFileSum(Map<String, Integer> orderFileSum) {
		this.orderFileSum = orderFileSum;
	}

}
