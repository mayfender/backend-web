package com.may.ple.backend.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ProgramFile {
	private String id;
	private String fileName;
	private Date createdDateTime;
	private Boolean isDeployer;
	private Boolean isTunnel;
	private Boolean isPython;
	private Long fileSize;
	private String command;

	public ProgramFile(String fileName, Date createdDateTime) {
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

	public Boolean getIsDeployer() {
		return isDeployer;
	}

	public void setIsDeployer(Boolean isDeployer) {
		this.isDeployer = isDeployer;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public Boolean getIsTunnel() {
		return isTunnel;
	}

	public void setIsTunnel(Boolean isTunnel) {
		this.isTunnel = isTunnel;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Boolean getIsPython() {
		return isPython;
	}

	public void setIsPython(Boolean isPython) {
		this.isPython = isPython;
	}

}
