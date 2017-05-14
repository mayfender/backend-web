package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.BatchNoticeFile;

public class BatchNoticeFindCriteriaResp extends CommonCriteriaResp {
	private List<BatchNoticeFile> files;
	private String fileName;
	private Long totalItems;
	
	public BatchNoticeFindCriteriaResp(){}
	
	public BatchNoticeFindCriteriaResp(int statusCode) {
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

	public List<BatchNoticeFile> getFiles() {
		return files;
	}

	public void setFiles(List<BatchNoticeFile> files) {
		this.files = files;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
