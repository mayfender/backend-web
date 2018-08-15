package com.may.ple.backend.entity;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bson.types.ObjectId;

public class Chatting {
	private String id;
	private Date createdDateTime;
	private Date updatedDateTime;
	private List<ObjectId> members;
	private String lastMsg;
	
	public Chatting(){}

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

	public List<ObjectId> getMembers() {
		return members;
	}

	public void setMembers(List<ObjectId> members) {
		this.members = members;
	}

	public String getLastMsg() {
		return lastMsg;
	}

	public void setLastMsg(String lastMsg) {
		this.lastMsg = lastMsg;
	}
	
}
