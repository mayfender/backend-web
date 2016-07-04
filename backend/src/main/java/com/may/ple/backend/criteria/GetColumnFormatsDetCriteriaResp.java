package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.GroupData;

public class GetColumnFormatsDetCriteriaResp extends CommonCriteriaResp {
	private Map<Integer, List<ColumnFormat>> colFormMap;
	private List<GroupData> groupDatas;
	
	public GetColumnFormatsDetCriteriaResp(){}
	
	public GetColumnFormatsDetCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Map<Integer, List<ColumnFormat>> getColFormMap() {
		return colFormMap;
	}

	public void setColFormMap(Map<Integer, List<ColumnFormat>> colFormMap) {
		this.colFormMap = colFormMap;
	}

	public List<GroupData> getGroupDatas() {
		return groupDatas;
	}

	public void setGroupDatas(List<GroupData> groupDatas) {
		this.groupDatas = groupDatas;
	}
	
}
