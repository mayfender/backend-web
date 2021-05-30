package com.may.ple.backend.model;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.may.ple.backend.entity.Dealer;
import com.may.ple.backend.entity.SendRound;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AuthenticationResponse extends ModelBase {

	private static final long serialVersionUID = -6624726180748515507L;
	private String token;
	private String showname;
	private String username;
	private Collection<? extends GrantedAuthority> authorities;
	private byte[] photo;
	private Date serverDateTime;
	private String userId;
	private String firstName;
	private String lastName;
	private String title;
	private String companyName;
	private Integer workingTime;
	private String version;
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
	private List<Dealer> dealers;
	private Map period;
	private Boolean userNotFoundErr;
	private Map<String, Object> orderFile;
	private List<SendRound> sendRoundList;

	public AuthenticationResponse() {
		super();
	}

	public AuthenticationResponse(String token, String userId, String showname, String username,
									Collection<? extends GrantedAuthority> authorities, byte[] photo) {
		this.setToken(token);
		this.userId = userId;
		this.showname = showname;
		this.username = username;
		this.authorities = authorities;
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

	public List<Dealer> getDealers() {
		return dealers;
	}

	public void setDealers(List<Dealer> dealers) {
		this.dealers = dealers;
	}

	public Map getPeriod() {
		return period;
	}

	public void setPeriod(Map period) {
		this.period = period;
	}

	public Boolean getUserNotFoundErr() {
		return userNotFoundErr;
	}

	public void setUserNotFoundErr(Boolean userNotFoundErr) {
		this.userNotFoundErr = userNotFoundErr;
	}

	public Map<String, Object> getOrderFile() {
		return orderFile;
	}

	public void setOrderFile(Map<String, Object> orderFile) {
		this.orderFile = orderFile;
	}

	public List<SendRound> getSendRoundList() {
		return sendRoundList;
	}

	public void setSendRoundList(List<SendRound> sendRoundList) {
		this.sendRoundList = sendRoundList;
	}

}
