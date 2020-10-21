package com.may.ple.backend.entity;

import java.util.Date;

public class SendRound {
	private String id;
	private String name;
	private Date limitedTime;
	private Date createdDateTime;
	private Boolean enabled;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getLimitedTime() {
		return limitedTime;
	}
	public void setLimitedTime(Date limitedTime) {
		this.limitedTime = limitedTime;
	}
	public Date getCreatedDateTime() {
		return createdDateTime;
	}
	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

}
