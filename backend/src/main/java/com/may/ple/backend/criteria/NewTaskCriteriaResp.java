package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class NewTaskCriteriaResp extends CommonCriteriaResp {
	private List<SptImportFingerFile> fingerFiles;
	private Long totalItems;
	
	public NewTaskCriteriaResp(){}
	
	public NewTaskCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getTotalItems() {
		return totalItems;
	}
	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}
	public List<SptImportFingerFile> getFingerFiles() {
		return fingerFiles;
	}
	public void setFingerFiles(List<SptImportFingerFile> fingerFiles) {
		this.fingerFiles = fingerFiles;
	}

}
