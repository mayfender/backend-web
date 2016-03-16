package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name="spt_master_naming_det")
public class MasterNamingDetail implements Serializable {
	private static final long serialVersionUID = -460251283905783534L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long namingDetId;
	private String displayValue;
	private Integer status;
	private Long createdBy;
	private Date createdDate;
	private Long modifiedBy;
	private Long modifiedDate;
	private Long namingId;
	
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
