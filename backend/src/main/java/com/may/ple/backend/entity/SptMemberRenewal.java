package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class SptMemberRenewal implements Serializable {
	private static final long serialVersionUID = -4144871117313499379L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long renewalId;
	private String memberId;
	@Temporal(TemporalType.DATE)
	private Date registerDate;
	@Temporal(TemporalType.DATE)
	private Date expireDate;
	private Integer status;
	private Boolean isActive;
	private Integer payType;
	private Double price;
	private Date createdDateTime;
	private Date updatedDateTime;
	@ManyToOne
	@JoinColumn(name="member_type_id", referencedColumnName="memberTypeId")
	private SptMemberType memberType;
	@ManyToOne
	@JoinColumn(name="created_by", referencedColumnName="id")
	private Users createdBy;
	@ManyToOne
	@JoinColumn(name="updated_by", referencedColumnName="id")
	private Users updatedBy;
	@ManyToOne
	@JoinColumn(name="reg_id", referencedColumnName="regId")
	private SptRegistration registration;
	
	protected SptMemberRenewal() {}

	public SptMemberRenewal(String memberId, SptRegistration registration, Date registerDate, Date expireDate, Integer status,
			Boolean isActive, Integer payType, Double price, Date createdDateTime, Date updatedDateTime, SptMemberType memberType, Users createdBy,
			Users updatedBy) {
		this.memberId = memberId;
		this.registration = registration;
		this.registerDate = registerDate;
		this.expireDate = expireDate;
		this.status = status;
		this.isActive = isActive;
		this.createdDateTime = createdDateTime;
		this.updatedDateTime = updatedDateTime;
		this.memberType = memberType;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.payType = payType;
		this.price = price;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getRenewalId() {
		return renewalId;
	}
	public void setRenewalId(Long renewalId) {
		this.renewalId = renewalId;
	}
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public Date getRegisterDate() {
		return registerDate;
	}
	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}
	public Date getExpireDate() {
		return expireDate;
	}
	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
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
	public SptMemberType getMemberType() {
		return memberType;
	}
	public void setMemberType(SptMemberType memberType) {
		this.memberType = memberType;
	}
	public Users getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(Users createdBy) {
		this.createdBy = createdBy;
	}
	public Users getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(Users updatedBy) {
		this.updatedBy = updatedBy;
	}
	public SptRegistration getRegistration() {
		return registration;
	}
	public void setRegistration(SptRegistration registration) {
		this.registration = registration;
	}
	public Integer getPayType() {
		return payType;
	}
	public void setPayType(Integer payType) {
		this.payType = payType;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}

}
