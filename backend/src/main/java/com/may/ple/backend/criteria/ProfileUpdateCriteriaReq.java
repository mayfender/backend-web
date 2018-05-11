package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ProfileUpdateCriteriaReq {
	private String oldUserNameShow;
	private String oldUserName;
	private String newUserNameShow;
	private String newUserName;
	private String password;
	private String firstName;
	private String lastName;
	private String phoneNumber;
	private String phoneExt;
	private String imgContent;
	private String imgName;
	private Boolean isChangedImg;
	private String title;
	private List<String> productIds;
	
	@Override
	public String toString() {
		
		ReflectionToStringBuilder stringBuilder = new ReflectionToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
		stringBuilder.setAppendStatics(true);
		stringBuilder.setAppendTransients(true);
		stringBuilder.setExcludeFieldNames("imgContent");
		
		return stringBuilder.toString();
	}

	public String getOldUserName() {
		return oldUserName;
	}

	public void setOldUserName(String oldUserName) {
		this.oldUserName = oldUserName;
	}

	public String getNewUserName() {
		return newUserName;
	}

	public void setNewUserName(String newUserName) {
		this.newUserName = newUserName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getOldUserNameShow() {
		return oldUserNameShow;
	}

	public void setOldUserNameShow(String oldUserNameShow) {
		this.oldUserNameShow = oldUserNameShow;
	}

	public String getNewUserNameShow() {
		return newUserNameShow;
	}

	public void setNewUserNameShow(String newUserNameShow) {
		this.newUserNameShow = newUserNameShow;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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

	public Boolean getIsChangedImg() {
		return isChangedImg;
	}

	public void setIsChangedImg(Boolean isChangedImg) {
		this.isChangedImg = isChangedImg;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPhoneExt() {
		return phoneExt;
	}

	public void setPhoneExt(String phoneExt) {
		this.phoneExt = phoneExt;
	}

	public List<String> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<String> productIds) {
		this.productIds = productIds;
	}

}
