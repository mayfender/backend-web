package com.may.ple.backend.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

public class TraceWork {
	private String id;
	private String resultText;
	private String tel;
	private ObjectId actionCode;
	private ObjectId resultCode;
	private Date appointDate;
	private Date nextTimeDate;
	private String contractNo;
	private String idCardNo;
	private Date createdDateTime;
	private Date updatedDateTime;
	private String createdBy;
	private String updatedBy;
	private Double appointAmount;
	private ObjectId templateId;
	@Transient
	private String actionCodeText;
	@Transient
	private String resultCodeText;
	@Transient
	private String createdByText;
	
	public TraceWork(){}
	
	public TraceWork(String resultText, String tel, ObjectId actionCode, ObjectId resultCode, Date appointDate, Date nextTimeDate) {
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

	public Double getAppointAmount() {
		return appointAmount;
	}

	public void setAppointAmount(Double appointAmount) {
		this.appointAmount = appointAmount;
	}

	public ObjectId getActionCode() {
		return actionCode;
	}

	public void setActionCode(ObjectId actionCode) {
		this.actionCode = actionCode;
	}

	public ObjectId getResultCode() {
		return resultCode;
	}

	public void setResultCode(ObjectId resultCode) {
		this.resultCode = resultCode;
	}

	public String getIdCardNo() {
		return idCardNo;
	}

	public void setIdCardNo(String idCardNo) {
		this.idCardNo = idCardNo;
	}

	public ObjectId getTemplateId() {
		return templateId;
	}

	public void setTemplateId(ObjectId templateId) {
		this.templateId = templateId;
	}
	
}
