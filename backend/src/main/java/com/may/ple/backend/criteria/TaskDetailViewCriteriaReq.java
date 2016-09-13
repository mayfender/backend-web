package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TaskDetailViewCriteriaReq {
	private String id;
	private String productId;
	private Boolean isInit;
	private Integer traceCurrentPage;
	private Integer traceItemsPerPage;
	private Integer currentPagePayment;
	private Integer itemsPerPagePayment;
	
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

}
