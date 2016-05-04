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
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.criteria.PersistUserCriteriaReq;

@Entity
public class SptRegistration implements Serializable {
	private static final long serialVersionUID = 4629098319875294158L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long regId;
	private String memberId;
	private String firstname;
	private String lastname;
	private String firstnameEng;
	private String lastnameEng;
	private String citizenId;
	@Temporal(TemporalType.DATE)
	private Date birthday;
	private String fingerId;
	@Temporal(TemporalType.DATE)
	private Date registerDate;
	@Temporal(TemporalType.DATE)
	private Date expireDate;
	private String conTelNo;
	private String conMobileNo1;
	private String conMobileNo2;
	private String conMobileNo3;
	private String conLineId;
	private String conFacebook;
	private String conEmail;
	private String conAddress;
	private Integer status;
	private Long createdBy;
	private Long modifiedBy;
	private Long memberTypeId;
	private Long userId;
	private Long imgId;
	private Integer payType;
	private Double price;
	@ManyToOne
	@JoinColumn(name="prefix_name_id", referencedColumnName="namingDetId")
	private SptMasterNamingDet prefixName;
	@ManyToOne
	@JoinColumn(name="zipcode_id")
	private Zipcodes zipcode;
	@Transient
	private String memberTypeName;
	@Transient
	private Integer enabled;
	@Transient
	private PersistUserCriteriaReq authen;
	@Transient
	private String imgBase64;
	@Transient
	private String period;
	
	protected SptRegistration() {}
	
	public SptRegistration(Long regId, String firstname, String lastname, String memberTypeName, Integer enabled, String memberId, Date expireDate) {
		this.regId = regId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.memberTypeName = memberTypeName;
		this.enabled = enabled;
		this.memberId = memberId;
		this.expireDate = expireDate;
	}
	
	public SptRegistration(Long regId, String firstname, String lastname, String memberTypeName, Integer enabled, Date registerDate, 
			               Date expireDate, Long memberTypeId, Integer status) {
		this.regId = regId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.memberTypeName = memberTypeName;
		this.enabled = enabled;
		this.registerDate = registerDate;
		this.expireDate = expireDate;
		this.memberTypeId = memberTypeId;
		this.status = status;
	}
	
	public SptRegistration(String memberId, SptMasterNamingDet prefixName, String firstname, String lastname, String firstnameEng, String lastnameEng, String citizenId,
			Date birthday, String fingerId, Date registerDate, Date expireDate, String conTelNo,
			String conMobileNo1, String conMobileNo2, String conMobileNo3, String conLineId, String conFacebook, String conEmail, String conAddress,
			Integer status, Long createdBy, Long modifiedBy, Long memberTypeId, Long userId, Long imgId, Integer payType, Double price, Zipcodes zipcode) {
		this.memberId = memberId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.firstnameEng = firstnameEng;
		this.lastnameEng = lastnameEng;
		this.citizenId = citizenId;
		this.birthday = birthday;
		this.fingerId = fingerId;
		this.registerDate = registerDate;
		this.expireDate = expireDate;
		this.conTelNo = conTelNo;
		this.conMobileNo1 = conMobileNo1;
		this.conMobileNo2 = conMobileNo2;
		this.conMobileNo3 = conMobileNo3;
		this.conLineId = conLineId;
		this.conFacebook = conFacebook;
		this.conEmail = conEmail;
		this.conAddress = conAddress;
		this.status = status;
		this.createdBy = createdBy;
		this.modifiedBy = modifiedBy;
		this.memberTypeId = memberTypeId;
		this.userId = userId;
		this.imgId = imgId;
		this.prefixName = prefixName;
		this.payType = payType;
		this.price = price;
		this.zipcode = zipcode;
	}
	
	public SptRegistration(Long regId, String memberId, SptMasterNamingDet prefixName, String firstname, String lastname, String firstnameEng, String lastnameEng,
			String citizenId,
			Date birthday, String fingerId, Date expireDate, Date registerDate, String conTelNo,
			String conMobileNo1, String conMobileNo2, String conMobileNo3, String conLineId, String conFacebook, String conEmail, String conAddress,
			Long memberTypeId, String userName, String authority, Integer enabled, Long imgId, Integer payType, Double price, Zipcodes zipcode) {
		this.regId = regId;
		this.memberId = memberId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.firstnameEng = firstnameEng;
		this.lastnameEng = lastnameEng;
		this.citizenId = citizenId;
		this.birthday = birthday;
		this.fingerId = fingerId;
		this.expireDate = expireDate;
		this.registerDate = registerDate;
		this.conTelNo = conTelNo;
		this.conMobileNo1 = conMobileNo1;
		this.conMobileNo2 = conMobileNo2;
		this.conMobileNo3 = conMobileNo3;
		this.conLineId = conLineId;
		this.conFacebook = conFacebook;
		this.conEmail = conEmail;
		this.conAddress = conAddress;
		this.memberTypeId = memberTypeId;
		this.getAuthen().setUserName(userName);
		this.getAuthen().setAuthority(authority);
		this.getAuthen().setStatus(enabled);
		this.imgId = imgId;
		this.prefixName = prefixName;
		this.payType = payType;
		this.price = price;
		this.zipcode = zipcode;
	}

	@Override
	public String toString() {
		
		ReflectionToStringBuilder stringBuilder = new ReflectionToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
		stringBuilder.setAppendStatics(true);
		stringBuilder.setAppendTransients(true);
		stringBuilder.setExcludeFieldNames("imgBase64");
		
		return stringBuilder.toString();
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
	public Long getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public String getMemberTypeName() {
		return memberTypeName;
	}
	public void setMemberTypeName(String memberTypeName) {
		this.memberTypeName = memberTypeName;
	}
	public Integer getEnabled() {
		return enabled;
	}
	public void setEnabled(Integer enabled) {
		this.enabled = enabled;
	}
	public Long getImgId() {
		return imgId;
	}
	public void setImgId(Long imgId) {
		this.imgId = imgId;
	}
	public PersistUserCriteriaReq getAuthen() {
		if(authen == null) authen = new PersistUserCriteriaReq();
		return authen;
	}
	public void setAuthen(PersistUserCriteriaReq authen) {
		this.authen = authen;
	}
	public String getImgBase64() {
		return imgBase64;
	}
	public void setImgBase64(String imgBase64) {
		this.imgBase64 = imgBase64;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public SptMasterNamingDet getPrefixName() {
		return prefixName;
	}
	public void setPrefixName(SptMasterNamingDet prefixName) {
		this.prefixName = prefixName;
	}
	public String getConMobileNo1() {
		return conMobileNo1;
	}
	public void setConMobileNo1(String conMobileNo1) {
		this.conMobileNo1 = conMobileNo1;
	}
	public String getConMobileNo2() {
		return conMobileNo2;
	}
	public void setConMobileNo2(String conMobileNo2) {
		this.conMobileNo2 = conMobileNo2;
	}
	public String getConMobileNo3() {
		return conMobileNo3;
	}
	public void setConMobileNo3(String conMobileNo3) {
		this.conMobileNo3 = conMobileNo3;
	}
	public String getFirstnameEng() {
		return firstnameEng;
	}
	public void setFirstnameEng(String firstnameEng) {
		this.firstnameEng = firstnameEng;
	}
	public String getLastnameEng() {
		return lastnameEng;
	}
	public void setLastnameEng(String lastnameEng) {
		this.lastnameEng = lastnameEng;
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
	public Zipcodes getZipcode() {
		return zipcode;
	}
	public void setZipcode(Zipcodes zipcode) {
		this.zipcode = zipcode;
	}
	
}
