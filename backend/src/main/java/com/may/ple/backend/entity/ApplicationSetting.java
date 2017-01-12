package com.may.ple.backend.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ApplicationSetting {
	private String id;
	private String companyName;
	private String mongdumpPath;
	private String backupPath;
	private String backupUsername;
	private String backupPassword;
	private String phoneWsServer;
	private String phoneRealm;
	private String phoneDefaultPass;
	private String license;
	private String productKey;
	
	public ApplicationSetting(){}
	
	public ApplicationSetting(String companyName) {
		this.companyName = companyName;
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

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getMongdumpPath() {
		return mongdumpPath;
	}

	public void setMongdumpPath(String mongdumpPath) {
		this.mongdumpPath = mongdumpPath;
	}

	public String getBackupPath() {
		return backupPath;
	}

	public void setBackupPath(String backupPath) {
		this.backupPath = backupPath;
	}

	public String getBackupUsername() {
		return backupUsername;
	}

	public void setBackupUsername(String backupUsername) {
		this.backupUsername = backupUsername;
	}

	public String getBackupPassword() {
		return backupPassword;
	}

	public void setBackupPassword(String backupPassword) {
		this.backupPassword = backupPassword;
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

	public String getPhoneDefaultPass() {
		return phoneDefaultPass;
	}

	public void setPhoneDefaultPass(String phoneDefaultPass) {
		this.phoneDefaultPass = phoneDefaultPass;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getProductKey() {
		return productKey;
	}

	public void setProductKey(String productKey) {
		this.productKey = productKey;
	}

}
