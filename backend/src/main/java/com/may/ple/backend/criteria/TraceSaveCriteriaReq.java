package com.may.ple.backend.criteria;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.AddressNotice;

public class TraceSaveCriteriaReq {
	private String id;
	private String resultText;
	private String tel;
	private String actionCode;
	private String resultCode;
	private String productId;
	private Date appointDate;
	private Double appointAmount;
	private Date nextTimeDate;
	private String contractNo;
	private String idCardNo;
	private String taskDetailId;
	private String templateId;
	private AddressNotice addressNotice;
	private String addressNoticeStr;
	
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

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public String getTaskDetailId() {
		return taskDetailId;
	}

	public void setTaskDetailId(String taskDetailId) {
		this.taskDetailId = taskDetailId;
	}

	public Double getAppointAmount() {
		return appointAmount;
	}

	public void setAppointAmount(Double appointAmount) {
		this.appointAmount = appointAmount;
	}

	public String getIdCardNo() {
		return idCardNo;
	}

	public void setIdCardNo(String idCardNo) {
		this.idCardNo = idCardNo;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public AddressNotice getAddressNotice() {
		return addressNotice;
	}

	public void setAddressNotice(AddressNotice addressNotice) {
		this.addressNotice = addressNotice;
	}

	public String getAddressNoticeStr() {
		return addressNoticeStr;
	}

	public void setAddressNoticeStr(String addressNoticeStr) {
		this.addressNoticeStr = addressNoticeStr;
	}

}
