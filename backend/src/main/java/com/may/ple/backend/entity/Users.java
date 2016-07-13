package com.may.ple.backend.entity;


import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Users {
	@Id
	private String id;
	private String showname;
	private String username;
	@JsonIgnore
	private String password;
	private Boolean enabled;
	private Date createdDateTime;
	private Date updatedDateTime;
	private List<SimpleGrantedAuthority> authorities;
	private List<String> products;
	private UserSetting setting;
	private Integer order;
	private String firstName;
	private String lastName;
	private String phoneNumber;
	private ImgData imgData;
	private String createdBy;
	private String updatedBy;
	
	public Users() {}
	
	public Users(String showname, String username, String password, Date createdDateTime, Date updatedDateTime, Boolean enabled, 
			     List<SimpleGrantedAuthority> authorities, List<String> products, Integer order) {
		this.showname = showname;
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.createdDateTime = createdDateTime;
		this.updatedDateTime = updatedDateTime;
		this.authorities = authorities;
		this.products = products;
		this.order = order;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	@JsonIgnore
	public String getPassword() {
		return password;
	}
	@JsonProperty
	public void setPassword(String password) {
		this.password = password;
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
	public String getShowname() {
		return showname;
	}
	public void setShowname(String showname) {
		this.showname = showname;
	}
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public List<SimpleGrantedAuthority> getAuthorities() {
		return authorities;
	}
	public void setAuthorities(List<SimpleGrantedAuthority> authorities) {
		this.authorities = authorities;
	}
	public List<String> getProducts() {
		return products;
	}
	public void setProducts(List<String> products) {
		this.products = products;
	}

	public UserSetting getSetting() {
		return setting;
	}

	public void setSetting(UserSetting setting) {
		this.setting = setting;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
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

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public ImgData getImgData() {
		return imgData;
	}

	public void setImgData(ImgData imgData) {
		this.imgData = imgData;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	
}
