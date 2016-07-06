package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.GroupData;
import com.may.ple.backend.model.RelatedData;

public class TaskDetailViewCriteriaResp extends CommonCriteriaResp {
	private Map taskDetail;
	private Map<Integer, List<ColumnFormat>> ColFormMap;
	private List<GroupData> groupDatas;
	private Map<String, RelatedData> relatedData;
	
	public TaskDetailViewCriteriaResp(){}
	
	public TaskDetailViewCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Map getTaskDetail() {
		return taskDetail;
	}

	public void setTaskDetail(Map taskDetail) {
		this.taskDetail = taskDetail;
	}

	public List<GroupData> getGroupDatas() {
		return groupDatas;
	}

	public void setGroupDatas(List<GroupData> groupDatas) {
		this.groupDatas = groupDatas;
	}

	public Map<Integer, List<ColumnFormat>> getColFormMap() {
		return ColFormMap;
	}

	public void setColFormMap(Map<Integer, List<ColumnFormat>> colFormMap) {
		ColFormMap = colFormMap;
	}

	public Map<String, RelatedData> getRelatedData() {
		return relatedData;
	}

	public void setRelatedData(Map<String, RelatedData> relatedData) {
		this.relatedData = relatedData;
	}

}
