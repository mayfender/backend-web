package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class SptImportFingerFile implements Serializable {
	private static final long serialVersionUID = -4144871117313499379L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long fingerFileId;
	private String fileName;
	@Temporal(TemporalType.DATE)
	private Date createdDate;
	private Date startedDateTime;
	private Date endedDateTime;
	private Integer status;
		
	protected SptImportFingerFile() {}
	
	public SptImportFingerFile(String fileName, Date createdDate, Date startedDateTime, Date endedDateTime, Integer status) {
		this.fileName = fileName;
		this.createdDate = createdDate;
		this.startedDateTime = startedDateTime;
		this.endedDateTime = endedDateTime;
		this.status = status;
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
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
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
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Long getFingerFileId() {
		return fingerFileId;
	}
	public void setFingerFileId(Long fingerFileId) {
		this.fingerFileId = fingerFileId;
	}

}
