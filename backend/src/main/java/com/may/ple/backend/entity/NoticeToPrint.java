package com.may.ple.backend.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bson.types.ObjectId;

public class NoticeToPrint {
	private String id;
	private ObjectId noticeId;
	private ObjectId taskDetailId;
	private String address;
	private String customerName;
	private Date dateInput;
	
	public NoticeToPrint(){}

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

	public ObjectId getNoticeId() {
		return noticeId;
	}

	public void setNoticeId(ObjectId noticeId) {
		this.noticeId = noticeId;
	}

	public Date getDateInput() {
		return dateInput;
	}

	public void setDateInput(Date dateInput) {
		this.dateInput = dateInput;
	}

	public ObjectId getTaskDetailId() {
		return taskDetailId;
	}

	public void setTaskDetailId(ObjectId taskDetailId) {
		this.taskDetailId = taskDetailId;
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
	
}
