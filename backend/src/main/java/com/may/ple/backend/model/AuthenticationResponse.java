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
	private String phoneExt;
	private String title;
	private String companyName;
	private Integer workingTime;
	private String version;
	private String phoneWsServer;
	private String phoneRealm;
	private String phonePass;
	private Boolean isLicenseNotValid;
	private Integer licenseYearsRemain;
	private Integer licenseMonthsRemain;
	private Integer licenseDaysRemain;
	private String LicenseDetail;
	private Boolean isOutOfWorkingTime;
	private String productKey;
	private Boolean isDisabled;
	private Boolean WebExtractIsEnabled;
	private String warning;
	
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

	public String getPhoneExt() {
		return phoneExt;
	}

	public void setPhoneExt(String phoneExt) {
		this.phoneExt = phoneExt;
	}

	public String getPhoneWsServer() {
		return phoneWsServer;
	}

	public void setPhoneWsServer(String phoneWsServer) {
		this.phoneWsServer = phoneWsServer;
	}

	public String getPhoneRealm() {
		return phoneRealm;
	}

	public void setPhoneRealm(String phoneRealm) {
		this.phoneRealm = phoneRealm;
	}

	public String getPhonePass() {
		return phonePass;
	}

	public void setPhonePass(String phonePass) {
		this.phonePass = phonePass;
	}

	public Boolean getIsLicenseNotValid() {
		return isLicenseNotValid;
	}

	public void setIsLicenseNotValid(Boolean isLicenseNotValid) {
		this.isLicenseNotValid = isLicenseNotValid;
	}

	public Integer getLicenseDaysRemain() {
		return licenseDaysRemain;
	}

	public void setLicenseDaysRemain(Integer licenseDaysRemain) {
		this.licenseDaysRemain = licenseDaysRemain;
	}

	public String getLicenseDetail() {
		return LicenseDetail;
	}

	public void setLicenseDetail(String licenseDetail) {
		LicenseDetail = licenseDetail;
	}

	public Integer getLicenseYearsRemain() {
		return licenseYearsRemain;
	}

	public void setLicenseYearsRemain(Integer licenseYearsRemain) {
		this.licenseYearsRemain = licenseYearsRemain;
	}

	public Integer getLicenseMonthsRemain() {
		return licenseMonthsRemain;
	}

	public void setLicenseMonthsRemain(Integer licenseMonthsRemain) {
		this.licenseMonthsRemain = licenseMonthsRemain;
	}

	public Boolean getIsOutOfWorkingTime() {
		return isOutOfWorkingTime;
	}

	public void setIsOutOfWorkingTime(Boolean isOutOfWorkingTime) {
		this.isOutOfWorkingTime = isOutOfWorkingTime;
	}

	public String getProductKey() {
		return productKey;
	}

	public void setProductKey(String productKey) {
		this.productKey = productKey;
	}

	public Boolean getIsDisabled() {
		return isDisabled;
	}

	public void setIsDisabled(Boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	public Boolean getWebExtractIsEnabled() {
		return WebExtractIsEnabled;
	}

	public void setWebExtractIsEnabled(Boolean webExtractIsEnabled) {
		WebExtractIsEnabled = webExtractIsEnabled;
	}

	public String getWarning() {
		return warning;
	}

	public void setWarning(String warning) {
		this.warning = warning;
	}

}
