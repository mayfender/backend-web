package com.may.ple.backend.model;

import java.util.List;
import java.util.Map;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.GroupData;

public class RelatedData {
	private Map<Integer, List<ColumnFormat>> othersColFormMap;
	private List<GroupData> othersGroupDatas;
	private List<Map> othersData;
	
	public Map<Integer, List<ColumnFormat>> getOthersColFormMap() {
		return othersColFormMap;
	}

	public void setOthersColFormMap(Map<Integer, List<ColumnFormat>> othersColFormMap) {
		this.othersColFormMap = othersColFormMap;
	}

	public List<GroupData> getOthersGroupDatas() {
		return othersGroupDatas;
	}

	public void setOthersGroupDatas(List<GroupData> othersGroupDatas) {
		this.othersGroupDatas = othersGroupDatas;
	}

	public List<Map> getOthersData() {
		return othersData;
	}

	public void setOthersData(List<Map> othersData) {
		this.othersData = othersData;
	}

}
