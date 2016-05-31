package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.NewTaskFile;

public class NewTaskCriteriaResp extends CommonCriteriaResp {
	private List<NewTaskFile> fingerFiles;
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
	public List<NewTaskFile> getFingerFiles() {
		return fingerFiles;
	}
	public void setFingerFiles(List<NewTaskFile> fingerFiles) {
		this.fingerFiles = fingerFiles;
	}

}
