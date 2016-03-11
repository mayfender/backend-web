package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class SptRegistration implements Serializable {
	private static final long serialVersionUID = 4629098319875294158L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long regId;
	private String memberId;
	private String prefixName;
	private String firstname;
	private String lastname;
	private String citizenId;
	private Date birthday;
	private String fingerId;
	private String picturePath;
	private Date registerDate;
	private Date expireDate;
	private String conTelNo;
	private String conMobileNo;
	private String conLineId;
	private String conFacebook;
	private String conEmail;
	private String conAddress;
	private Integer isActive;
	private Integer status;
	private Long createdBy;
	private Date createdDate;
	private Long modifiedBy;
	private Date modifiedDate;
	private Long memberTypeId;
	private Long userId;
	@Transient
	private String memberTypeName;
	
	protected SptRegistration() {}
	
	public SptRegistration(Long regId, String firstname, String lastname, Integer isActive, String memberTypeName) {
		this.regId = regId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.isActive = isActive;
		this.memberTypeName = memberTypeName;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	
	public Long getRegId() {
		return regId;
	}
	public void setRegId(Long regId) {
		this.regId = regId;
	}
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getPrefixName() {
		return prefixName;
	}
	public void setPrefixName(String prefixName) {
		this.prefixName = prefixName;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getCitizenId() {
		return citizenId;
	}
	public void setCitizenId(String citizenId) {
		this.citizenId = citizenId;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public String getFingerId() {
		return fingerId;
	}
	public void setFingerId(String fingerId) {
		this.fingerId = fingerId;
	}
	public String getPicturePath() {
		return picturePath;
	}
	public void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getMemberTypeId() {
		return memberTypeId;
	}
	public void setMemberTypeId(Long memberTypeId) {
		this.memberTypeId = memberTypeId;
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
	public String getConTelNo() {
		return conTelNo;
	}
	public void setConTelNo(String conTelNo) {
		this.conTelNo = conTelNo;
	}
	public String getConMobileNo() {
		return conMobileNo;
	}
	public void setConMobileNo(String conMobileNo) {
		this.conMobileNo = conMobileNo;
	}
	public String getConLineId() {
		return conLineId;
	}
	public void setConLineId(String conLineId) {
		this.conLineId = conLineId;
	}
	public String getConFacebook() {
		return conFacebook;
	}
	public void setConFacebook(String conFacebook) {
		this.conFacebook = conFacebook;
	}
	public String getConEmail() {
		return conEmail;
	}
	public void setConEmail(String conEmail) {
		this.conEmail = conEmail;
	}
	public String getConAddress() {
		return conAddress;
	}
	public void setConAddress(String conAddress) {
		this.conAddress = conAddress;
	}
	public Integer getIsActive() {
		return isActive;
	}
	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
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
	public String getMemberTypeName() {
		return memberTypeName;
	}
	public void setMemberTypeName(String memberTypeName) {
		this.memberTypeName = memberTypeName;
	}
	
}
