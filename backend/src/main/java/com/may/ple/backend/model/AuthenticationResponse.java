package com.may.ple.backend.model;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.UserSetting;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AuthenticationResponse extends ModelBase {

	private static final long serialVersionUID = -6624726180748515507L;
	private String token;
	private String showname;
	private String username;
	private Collection<? extends GrantedAuthority> authorities;
	private List<Product> products;
	private UserSetting setting;
	private byte[] photo;
	private Date serverDateTime;
	private String userId;
	private String firstName;
	private String lastName;
	private String phoneNumber;
	private String title;
	private String companyName;
	private Integer workingTime;
	private String version;
	
	public AuthenticationResponse() {
		super();
	}

	public AuthenticationResponse(String token, String userId, String showname, String username, Collection<? extends GrantedAuthority> authorities, 
								  List<Product> products, UserSetting setting, byte[] photo) {
		this.setToken(token);
		this.userId = userId;
		this.showname = showname;
		this.username = username;
		this.authorities = authorities;
		this.products = products;
		this.setting = setting;
		this.photo = photo;
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getShowname() {
		return showname;
	}

	public void setShowname(String showname) {
		this.showname = showname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public UserSetting getSetting() {
		return setting;
	}

	public void setSetting(UserSetting setting) {
		this.setting = setting;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public Date getServerDateTime() {
		return serverDateTime;
	}

	public void setServerDateTime(Date serverDateTime) {
		this.serverDateTime = serverDateTime;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getWorkingTime() {
		return workingTime;
	}

	public void setWorkingTime(Integer workingTime) {
		this.workingTime = workingTime;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
