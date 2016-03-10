package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SptMemberType implements Serializable {
	private static final long serialVersionUID = 8902254075756411333L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long memberTypeId;
	private String memberTypeName;
	private Integer durationType;
	private Integer durationQty;
	private Double memberPrice;
	private Integer status;
	private Long createdBy;
	private Date createdDate;
	private Long modifiedBy;
	private Date modifiedDate;
	
	protected SptMemberType() {}
	
	public SptMemberType(Integer status, Long createdBy, Date createdDate, Long modifiedBy, Date modifiedDate, 
						 String memberTypeName, Integer durationType, Integer durationQty, Double memberPrice) {
		this.status = status;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.modifiedBy = modifiedBy;
		this.modifiedDate = modifiedDate;
		this.memberTypeName = memberTypeName;
		this.durationType = durationType;
		this.durationQty = durationQty;
		this.memberPrice = memberPrice;
	}
	
	public SptMemberType(Long memberTypeId, String memberTypeName, Integer status) {
		this.memberTypeId = memberTypeId;
		this.memberTypeName = memberTypeName;
		this.status = status;
	}
	
	public Long getMemberTypeId() {
		return memberTypeId;
	}
	public void setMemberTypeId(Long memberTypeId) {
		this.memberTypeId = memberTypeId;
	}
	public String getMemberTypeName() {
		return memberTypeName;
	}
	public void setMemberTypeName(String memberTypeName) {
		this.memberTypeName = memberTypeName;
	}
	public Integer getDurationType() {
		return durationType;
	}
	public void setDurationType(Integer durationType) {
		this.durationType = durationType;
	}
	public Integer getDurationQty() {
		return durationQty;
	}
	public void setDurationQty(Integer durationQty) {
		this.durationQty = durationQty;
	}
	public Double getMemberPrice() {
		return memberPrice;
	}
	public void setMemberPrice(Double memberPrice) {
		this.memberPrice = memberPrice;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
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
	
}
