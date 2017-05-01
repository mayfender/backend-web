package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.NoticeXDocFile;

public class NoticeXDocFindCriteriaResp extends CommonCriteriaResp {
	private List<NoticeXDocFile> files;
	private Long totalItems;
	
	public NoticeXDocFindCriteriaResp(){}
	
	public NoticeXDocFindCriteriaResp(int statusCode) {
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

	public List<NoticeXDocFile> getFiles() {
		return files;
	}

	public void setFiles(List<NoticeXDocFile> files) {
		this.files = files;
	}

}
