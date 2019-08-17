package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.EngineTpl;

public class EngTplCriteriaResp extends CommonCriteriaResp {
	private List<EngineTpl> files;
	private Long totalItems;
	
	public EngTplCriteriaResp() {}
	
	public EngTplCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<EngineTpl> getFiles() {
		return files;
	}

	public void setFiles(List<EngineTpl> files) {
		this.files = files;
	}

	public Long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

}
