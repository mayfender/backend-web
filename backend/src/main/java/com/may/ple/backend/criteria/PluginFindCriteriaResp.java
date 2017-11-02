package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.PluginFile;

public class PluginFindCriteriaResp extends CommonCriteriaResp {
	private List<PluginFile> files;
	private Long totalItems;
	
	public PluginFindCriteriaResp(){}
	
	public PluginFindCriteriaResp(int statusCode) {
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
	public List<PluginFile> getFiles() {
		return files;
	}
	public void setFiles(List<PluginFile> files) {
		this.files = files;
	}

}
