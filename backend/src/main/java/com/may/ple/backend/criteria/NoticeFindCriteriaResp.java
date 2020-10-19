package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.NoticeFile;

public class NoticeFindCriteriaResp extends CommonCriteriaResp {
	private List<NoticeFile> files;
	private Long totalItems;
	
	public NoticeFindCriteriaResp(){}
	
	public NoticeFindCriteriaResp(int statusCode) {
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

	public List<NoticeFile> getFiles() {
		return files;
	}

	public void setFiles(List<NoticeFile> files) {
		this.files = files;
	}

}