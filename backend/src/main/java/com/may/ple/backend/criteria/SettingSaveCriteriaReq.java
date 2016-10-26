package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SettingSaveCriteriaReq {
	private String companyName;
	private String mongdumpPath;
	private String backupPath;
	private String backupTime;
	private String backupUsername;
	private String backupPassword;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
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

	public String getBackupTime() {
		return backupTime;
	}

	public void setBackupTime(String backupTime) {
		this.backupTime = backupTime;
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

}
