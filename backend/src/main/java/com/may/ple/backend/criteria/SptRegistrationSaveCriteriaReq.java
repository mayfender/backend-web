package com.may.ple.backend.criteria;

import java.util.Date;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.SptMasterNamingDet;

public class SptRegistrationSaveCriteriaReq {
	private Long regId;
	private PersistUserCriteriaReq authen;
	private String firstname;
	private String lastname;
	private String firstnameEng;
	private String lastnameEng;
	private Date birthday;
	private String citizenId;
	private String fingerId;
	private String conTelNo;
	private String conMobileNo1;
	private String conMobileNo2;
	private String conMobileNo3;
	private String conEmail;
	private String conAddress;
	private String conLineId;
	private String conFacebook;
	private Long memberTypeId;
	private Date expireDate;	
	private Date registerDate;	
	private String imgContent;
	private String imgName;
	private Boolean isChangedImg;
	private SptMasterNamingDet prefixName;
	private Integer payType;
	private Double price;
	
	@Override
	public String toString() {
		
		ReflectionToStringBuilder stringBuilder = new ReflectionToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
		stringBuilder.setAppendStatics(true);
		stringBuilder.setAppendTransients(true);
		stringBuilder.setExcludeFieldNames("imgContent");
		
		return stringBuilder.toString();
	}
	
	public PersistUserCriteriaReq getAuthen() {
		if(authen == null) authen = new PersistUserCriteriaReq();
		return authen;
	}
	public void setAuthen(PersistUserCriteriaReq authen) {
		this.authen = authen;
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
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public String getCitizenId() {
		return citizenId;
	}
	public void setCitizenId(String citizenId) {
		this.citizenId = citizenId;
	}
	public String getFingerId() {
		return fingerId;
	}
	public void setFingerId(String fingerId) {
		this.fingerId = fingerId;
	}
	public String getConTelNo() {
		return conTelNo;
	}
	public void setConTelNo(String conTelNo) {
		this.conTelNo = conTelNo;
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
	public Long getMemberTypeId() {
		return memberTypeId;
	}
	public void setMemberTypeId(Long memberTypeId) {
		this.memberTypeId = memberTypeId;
	}
	public Date getExpireDate() {
		return expireDate;
	}
	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}
	public String getImgContent() {
		return imgContent;
	}
	public void setImgContent(String imgContent) {
		this.imgContent = imgContent;
	}
	public String getImgName() {
		return imgName;
	}
	public void setImgName(String imgName) {
		this.imgName = imgName;
	}
	public Long getRegId() {
		return regId;
	}
	public void setRegId(Long regId) {
		this.regId = regId;
	}
	public Boolean getIsChangedImg() {
		return isChangedImg;
	}
	public void setIsChangedImg(Boolean isChangedImg) {
		this.isChangedImg = isChangedImg;
	}
	public Date getRegisterDate() {
		return registerDate;
	}
	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}
	public SptMasterNamingDet getPrefixName() {
		if(prefixName == null) prefixName = new SptMasterNamingDet();
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
	
}
