package com.may.ple.backend.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Transient;

public class TraceWork {
	private String id;
	private String resultText;
	private String tel;
	private String actionCode;
	private String resultCode;
	private Date appointDate;
	private Date nextTimeDate;
	private String contractNo;
	private Date createdDateTime;
	private Date updatedDateTime;
	private String createdBy;
	private String updatedBy;
	@Transient
	private String actionCodeText;
	@Transient
	private String resultCodeText;
	@Transient
	private String createdByText;
	
	public TraceWork(){}
	
	public TraceWork(String resultText, String tel, String actionCode, String resultCode, Date appointDate, Date nextTimeDate) {
		this.resultText = resultText;
		this.tel = tel;
		this.actionCode = actionCode;
		this.resultCode = resultCode;
		this.appointDate = appointDate;
		this.nextTimeDate = nextTimeDate;
	}

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

	public String getResultText() {
		return resultText;
	}

	public void setResultText(String resultText) {
		this.resultText = resultText;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public Date getAppointDate() {
		return appointDate;
	}

	public void setAppointDate(Date appointDate) {
		this.appointDate = appointDate;
	}

	public Date getNextTimeDate() {
		return nextTimeDate;
	}

	public void setNextTimeDate(Date nextTimeDate) {
		this.nextTimeDate = nextTimeDate;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public Date getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public Date getUpdatedDateTime() {
		return updatedDateTime;
	}

	public void setUpdatedDateTime(Date updatedDateTime) {
		this.updatedDateTime = updatedDateTime;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getActionCodeText() {
		return actionCodeText;
	}

	public void setActionCodeText(String actionCodeText) {
		this.actionCodeText = actionCodeText;
	}

	public String getResultCodeText() {
		return resultCodeText;
	}

	public void setResultCodeText(String resultCodeText) {
		this.resultCodeText = resultCodeText;
	}

	public String getCreatedByText() {
		return createdByText;
	}

	public void setCreatedByText(String createdByText) {
		this.createdByText = createdByText;
	}
	
}
