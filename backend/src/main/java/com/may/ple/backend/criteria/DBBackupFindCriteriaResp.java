package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DBBackupFindCriteriaResp extends CommonCriteriaResp {
	private List<String> dirList;
	private List<String> fileList;
	
	public DBBackupFindCriteriaResp(){}
	
	public DBBackupFindCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<String> getDirList() {
		return dirList;
	}

	public void setDirList(List<String> dirList) {
		this.dirList = dirList;
	}

	public List<String> getFileList() {
		return fileList;
	}

	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}

}
