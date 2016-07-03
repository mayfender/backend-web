package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;

public class GetColumnFormatsDetCriteriaResp extends CommonCriteriaResp {
	private List<String> groupNames;
	private Map<String, List<ColumnFormat>> colFormMap;
	
	public GetColumnFormatsDetCriteriaResp(){}
	
	public GetColumnFormatsDetCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<String> getGroupNames() {
		return groupNames;
	}

	public void setGroupNames(List<String> groupNames) {
		this.groupNames = groupNames;
	}

	public Map<String, List<ColumnFormat>> getColFormMap() {
		return colFormMap;
	}

	public void setColFormMap(Map<String, List<ColumnFormat>> colFormMap) {
		this.colFormMap = colFormMap;
	}
	
}
