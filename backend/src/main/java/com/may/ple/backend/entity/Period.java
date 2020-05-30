package com.may.ple.backend.entity;

import java.util.Date;

public class Period {
	private String id;	
	private Date periodDateTime;
	private Date createdDateTime;
	
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
	
}
