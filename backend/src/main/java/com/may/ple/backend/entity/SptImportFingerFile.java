package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class SptImportFingerFile implements Serializable {
	private static final long serialVersionUID = -4144871117313499379L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long fingerFileId;
	private String fileName;
	private Date createdDateTime;
	private Date startedDateTime;
	private Date endedDateTime;
		
	protected SptImportFingerFile() {}
	
	public SptImportFingerFile(String fileName, Date createdDateTime, Date startedDateTime, Date endedDateTime) {
		this.fileName = fileName;
		this.createdDateTime = createdDateTime;
		this.startedDateTime = startedDateTime;
		this.endedDateTime = endedDateTime;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
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
	public Date getStartedDateTime() {
		return startedDateTime;
	}
	public void setStartedDateTime(Date startedDateTime) {
		this.startedDateTime = startedDateTime;
	}
	public Date getEndedDateTime() {
		return endedDateTime;
	}
	public void setEndedDateTime(Date endedDateTime) {
		this.endedDateTime = endedDateTime;
	}
	public Long getFingerFileId() {
		return fingerFileId;
	}
	public void setFingerFileId(Long fingerFileId) {
		this.fingerFileId = fingerFileId;
	}

}
