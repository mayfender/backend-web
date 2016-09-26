package com.may.ple.backend.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TraceWorkComment {
	private String id;
	private String comment;
	private String contractNo;
	private String createdBy;
	private String updatedBy;
	private Date createdDateTime;
	private Date updatedDateTime;
	
	public TraceWorkComment(){}
	
	public TraceWorkComment(String comment, String contractNo, String createdBy, String updatedBy, Date createdDateTime, Date updatedDateTime) {
		this.comment = comment;
		this.contractNo = contractNo;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.createdDateTime = createdDateTime;
		this.updatedDateTime = updatedDateTime;
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
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
