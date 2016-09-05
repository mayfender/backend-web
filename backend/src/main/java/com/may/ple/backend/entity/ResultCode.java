package com.may.ple.backend.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ResultCode {
	private String id;
	private String rstCode;
	private String rstDesc;
	private String rstMeaning;
	private Integer enabled;
	private Date createdDateTime;
	private Date updatedDateTime;
	private String createdBy;
	private String updatedBy;
	private String resultGroupId;
	
	public ResultCode(){}
	
	public ResultCode(String rstCode, String rstDesc, String rstMeaning, Integer enabled) {
		this.rstCode = rstCode;
		this.rstDesc = rstDesc;
		this.rstMeaning = rstMeaning;
		this.enabled = enabled;
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

	public Integer getEnabled() {
		return enabled;
	}

	public void setEnabled(Integer enabled) {
		this.enabled = enabled;
	}

	public String getResultGroupId() {
		return resultGroupId;
	}

	public void setResultGroupId(String resultGroupId) {
		this.resultGroupId = resultGroupId;
	}

	public String getRstCode() {
		return rstCode;
	}

	public void setRstCode(String rstCode) {
		this.rstCode = rstCode;
	}

	public String getRstDesc() {
		return rstDesc;
	}

	public void setRstDesc(String rstDesc) {
		this.rstDesc = rstDesc;
	}

	public String getRstMeaning() {
		return rstMeaning;
	}

	public void setRstMeaning(String rstMeaning) {
		this.rstMeaning = rstMeaning;
	}
	
}
