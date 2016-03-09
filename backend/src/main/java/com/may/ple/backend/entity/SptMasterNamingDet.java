package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SptMasterNamingDet implements Serializable {
	private static final long serialVersionUID = -460251283905783534L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long namingDetId;
	private String displayValue;
	private Integer status;
	private Long createdBy;
	private Date createdDate;
	private Long modifiedBy;
	private Date modifiedDate;
	private Long namingId;
	
	protected SptMasterNamingDet() {}
	
	public SptMasterNamingDet(String displayValue, Integer status, Long createdBy, Date createdDate, Long modifiedBy, Date modifiedDate, Long namingId) {
		this.displayValue = displayValue;
		this.status = status;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.modifiedBy = modifiedBy;
		this.modifiedDate = modifiedDate;
		this.namingId = namingId;
	}
	
	public Long getNamingDetId() {
		return namingDetId;
	}
	public void setNamingDetId(Long namingDetId) {
		this.namingDetId = namingDetId;
	}
	public Long getNamingId() {
		return namingId;
	}
	public void setNamingId(Long namingId) {
		this.namingId = namingId;
	}
	public String getDisplayValue() {
		return displayValue;
	}
	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
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
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
}
