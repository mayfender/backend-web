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
public class SptMasterNaming implements Serializable {
	private static final long serialVersionUID = 591328794131409593L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long namingId;
	private String labelNameTh;
	private String labelNameEn;
	private Integer status;
	private Long createdBy;
	private Date createdDate;
	private Long modifiedBy;
	private Long modifiedDate;
	
	protected SptMasterNaming() {}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	
	public Long getNamingId() {
		return namingId;
	}
	public void setNamingId(Long namingId) {
		this.namingId = namingId;
	}
	public String getLabelNameTh() {
		return labelNameTh;
	}
	public void setLabelNameTh(String labelNameTh) {
		this.labelNameTh = labelNameTh;
	}
	public String getLabelNameEn() {
		return labelNameEn;
	}
	public void setLabelNameEn(String labelNameEn) {
		this.labelNameEn = labelNameEn;
	}
	public Long getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Long getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public Long getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Long modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
}
