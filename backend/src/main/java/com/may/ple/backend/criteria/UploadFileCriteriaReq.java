package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class UploadFileCriteriaReq {
	private String id;
	private String previousImgId;
	private String periodId;
	private String dealerId;
	private Integer currentPage;
	private Integer itemsPerPage;
	private String fileName;
	private String customerName;
	private Integer status;
	private Boolean isIncludeImg;
	private String orderFileId;

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

	public String getPeriodId() {
		return periodId;
	}

	public void setPeriodId(String periodId) {
		this.periodId = periodId;
	}

	public Integer getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public Integer getItemsPerPage() {
		return itemsPerPage;
	}

	public void setItemsPerPage(Integer itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	public String getDealerId() {
		return dealerId;
	}

	public void setDealerId(String dealerId) {
		this.dealerId = dealerId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public Boolean getIsIncludeImg() {
		return isIncludeImg;
	}

	public void setIsIncludeImg(Boolean isIncludeImg) {
		this.isIncludeImg = isIncludeImg;
	}

	public String getPreviousImgId() {
		return previousImgId;
	}

	public void setPreviousImgId(String previousImgId) {
		this.previousImgId = previousImgId;
	}

	public String getOrderFileId() {
		return orderFileId;
	}

	public void setOrderFileId(String orderFileId) {
		this.orderFileId = orderFileId;
	}

}
