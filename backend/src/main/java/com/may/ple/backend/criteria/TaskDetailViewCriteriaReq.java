package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TaskDetailViewCriteriaReq {
	private String id;
	private String contractNo;
	private List<String> ids;
	private String productId;
	private Boolean isInit;
	private Integer traceCurrentPage;
	private Integer traceItemsPerPage;
	private Integer currentPagePayment;
	private Integer itemsPerPagePayment;
	private Boolean isOldTrace;
	private Boolean isLog;

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

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public Boolean getIsInit() {
		return isInit;
	}

	public void setIsInit(Boolean isInit) {
		this.isInit = isInit;
	}

	public Integer getTraceCurrentPage() {
		return traceCurrentPage;
	}

	public void setTraceCurrentPage(Integer traceCurrentPage) {
		this.traceCurrentPage = traceCurrentPage;
	}

	public Integer getTraceItemsPerPage() {
		return traceItemsPerPage;
	}

	public void setTraceItemsPerPage(Integer traceItemsPerPage) {
		this.traceItemsPerPage = traceItemsPerPage;
	}

	public Integer getCurrentPagePayment() {
		return currentPagePayment;
	}

	public void setCurrentPagePayment(Integer currentPagePayment) {
		this.currentPagePayment = currentPagePayment;
	}

	public Integer getItemsPerPagePayment() {
		return itemsPerPagePayment;
	}

	public void setItemsPerPagePayment(Integer itemsPerPagePayment) {
		this.itemsPerPagePayment = itemsPerPagePayment;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public Boolean getIsOldTrace() {
		return isOldTrace;
	}

	public void setIsOldTrace(Boolean isOldTrace) {
		this.isOldTrace = isOldTrace;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public Boolean getIsLog() {
		return isLog;
	}

	public void setIsLog(Boolean isLog) {
		this.isLog = isLog;
	}

}
