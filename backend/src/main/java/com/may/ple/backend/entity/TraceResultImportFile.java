package com.may.ple.backend.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TraceResultImportFile {
	private String id;
	private String fileName;
	private String createdBy;
	private String updatedBy;
	private Date createdDateTime;
	private Date updateedDateTime;
	private Integer rowNum;
	private Boolean isOldTrace;
	private Boolean isAPIUpload;

	public TraceResultImportFile(String fileName, Date createdDateTime) {
		this.fileName = fileName;
		this.createdDateTime = createdDateTime;
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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Date getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
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

	public Date getUpdateedDateTime() {
		return updateedDateTime;
	}

	public void setUpdateedDateTime(Date updateedDateTime) {
		this.updateedDateTime = updateedDateTime;
	}

	public Integer getRowNum() {
		return rowNum;
	}

	public void setRowNum(Integer rowNum) {
		this.rowNum = rowNum;
	}

	public Boolean getIsOldTrace() {
		return isOldTrace;
	}

	public void setIsOldTrace(Boolean isOldTrace) {
		this.isOldTrace = isOldTrace;
	}

	public Boolean getIsAPIUpload() {
		return isAPIUpload;
	}

	public void setIsAPIUpload(Boolean isAPIUpload) {
		this.isAPIUpload = isAPIUpload;
	}

}