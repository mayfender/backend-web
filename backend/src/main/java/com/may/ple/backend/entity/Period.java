package com.may.ple.backend.entity;

import java.util.Date;

import org.bson.types.ObjectId;

public class Period {
	private String id;	
	private Date periodDateTime;
	private Date createdDateTime;
	private Date updatedDateTime;
	private ObjectId userId;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getPeriodDateTime() {
		return periodDateTime;
	}
	public void setPeriodDateTime(Date periodDateTime) {
		this.periodDateTime = periodDateTime;
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
	public ObjectId getUserId() {
		return userId;
	}
	public void setUserId(ObjectId userId) {
		this.userId = userId;
	}
	
}
