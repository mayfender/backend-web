package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DBBackupFindCriteriaReq extends CommonCriteriaResp {
	private Boolean isInit;
	private String dir;
	private String fileName;
	private Boolean isSystemFile;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Boolean getIsInit() {
		return isInit;
	}

	public void setIsInit(Boolean isInit) {
		this.isInit = isInit;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Boolean getIsSystemFile() {
		return isSystemFile;
	}

	public void setIsSystemFile(Boolean isSystemFile) {
		this.isSystemFile = isSystemFile;
	}

}
