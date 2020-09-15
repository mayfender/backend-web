package com.may.ple.backend.entity;

import java.util.Date;
import java.util.Map;

import org.bson.types.ObjectId;

public class OrderGroup {
	private String id;
	private String type;
	private ObjectId periodId;
	private Map families;
	private Date createdDateTime;
	private Date updatedDateTime;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public ObjectId getPeriodId() {
		return periodId;
	}
	public void setPeriodId(ObjectId periodId) {
		this.periodId = periodId;
	}
	public Map getFamilies() {
		return families;
	}
	public void setFamilies(Map families) {
		this.families = families;
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

}
