package com.may.ple.backend.criteria;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TraceSaveCriteriaReq {
	private String id;
	private String resultText;
	private String tel;
	private String actionCode;
	private String resultCode;
	private String productId;
	private Date appointDate;
	private Date nextTimeDate;
	private String taskDetailId;
	
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

	public String getTaskDetailId() {
		return taskDetailId;
	}

	public void setTaskDetailId(String taskDetailId) {
		this.taskDetailId = taskDetailId;
	}

}
