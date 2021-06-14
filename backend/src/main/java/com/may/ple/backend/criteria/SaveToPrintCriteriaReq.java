package com.may.ple.backend.criteria;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SaveToPrintCriteriaReq {
	private String noticeId;
	private Date dateInput;
	private String taskDetailId;
	private String productId;
	private String address;
	private String customerName;
	private Boolean printStatus;
	private String batchNoticeFileId;
	private Boolean isLog;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getNoticeId() {
		return noticeId;
	}

	public void setNoticeId(String noticeId) {
		this.noticeId = noticeId;
	}

	public Date getDateInput() {
		return dateInput;
	}

	public void setDateInput(Date dateInput) {
		this.dateInput = dateInput;
	}

	public String getTaskDetailId() {
		return taskDetailId;
	}

	public void setTaskDetailId(String taskDetailId) {
		this.taskDetailId = taskDetailId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public Boolean getPrintStatus() {
		return printStatus;
	}

	public void setPrintStatus(Boolean printStatus) {
		this.printStatus = printStatus;
	}

	public String getBatchNoticeFileId() {
		return batchNoticeFileId;
	}

	public void setBatchNoticeFileId(String batchNoticeFileId) {
		this.batchNoticeFileId = batchNoticeFileId;
	}

	public Boolean getIsLog() {
		return isLog;
	}

	public void setIsLog(Boolean isLog) {
		this.isLog = isLog;
	}

}
