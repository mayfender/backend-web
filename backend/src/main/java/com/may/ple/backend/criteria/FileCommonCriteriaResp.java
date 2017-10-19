package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;

public class FileCommonCriteriaResp extends CommonCriteriaResp {
	private List<Map> files;
	private Long totalItems;
	private List<ColumnFormat> colDateTypes;
	private List<String> colNotFounds;
	
	public FileCommonCriteriaResp(){}
	
	public FileCommonCriteriaResp(int statusCode) {
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

	public List<Map> getFiles() {
		return files;
	}

	public void setFiles(List<Map> files) {
		this.files = files;
	}

	public List<ColumnFormat> getColDateTypes() {
		return colDateTypes;
	}

	public void setColDateTypes(List<ColumnFormat> colDateTypes) {
		this.colDateTypes = colDateTypes;
	}

	public List<String> getColNotFounds() {
		return colNotFounds;
	}

	public void setColNotFounds(List<String> colNotFounds) {
		this.colNotFounds = colNotFounds;
	}

}
