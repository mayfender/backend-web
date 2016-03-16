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
public class SptMasterNamingDet implements Serializable {
	private static final long serialVersionUID = -460251283905783534L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long namingDetId;
	private String displayValue;
	private Integer isActive;
	private Long createdBy;
	private Date createdDate;
	private Long modifiedBy;
	private Date modifiedDate;
	private Long namingId;
	
	protected SptMasterNamingDet() {}
	
	public SptMasterNamingDet(String displayValue, Integer isActive, Long createdBy, Date createdDate, Long modifiedBy, Date modifiedDate, Long namingId) {
		this.displayValue = displayValue;
		this.isActive = isActive;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.modifiedBy = modifiedBy;
		this.modifiedDate = modifiedDate;
		this.namingId = namingId;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
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
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public Integer getIsActive() {
		return isActive;
	}
	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
	}
	
}
