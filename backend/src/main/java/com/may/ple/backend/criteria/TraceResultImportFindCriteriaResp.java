package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.TraceResultImportFile;

public class TraceResultImportFindCriteriaResp extends CommonCriteriaResp {
	private List<TraceResultImportFile> files;
	private Long totalItems;
	
	public TraceResultImportFindCriteriaResp(){}
	
	public TraceResultImportFindCriteriaResp(int statusCode) {
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

	public List<TraceResultImportFile> getFiles() {
		return files;
	}

	public void setFiles(List<TraceResultImportFile> files) {
		this.files = files;
	}

}
