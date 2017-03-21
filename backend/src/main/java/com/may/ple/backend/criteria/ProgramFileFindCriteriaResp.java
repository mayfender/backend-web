package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ProgramFile;

public class ProgramFileFindCriteriaResp extends CommonCriteriaResp {
	private List<ProgramFile> files;
	private Long totalItems;
	
	public ProgramFileFindCriteriaResp(){}
	
	public ProgramFileFindCriteriaResp(int statusCode) {
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
	public List<ProgramFile> getFiles() {
		return files;
	}
	public void setFiles(List<ProgramFile> files) {
		this.files = files;
	}

}
