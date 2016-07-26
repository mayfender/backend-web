package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ActionCode;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.GroupData;
import com.may.ple.backend.entity.ResultCode;
import com.may.ple.backend.entity.ResultCodeGroup;
import com.may.ple.backend.model.RelatedData;

public class TaskDetailViewCriteriaResp extends CommonCriteriaResp {
	private Map taskDetail;
	private Map<Integer, List<ColumnFormat>> ColFormMap;
	private List<GroupData> groupDatas;
	private Map<String, RelatedData> relatedData;
	private List<ActionCode> actionCodes;
	private List<ResultCode> resultCodes;
	private List<ResultCodeGroup> resultCodeGroups;
	
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

	public List<ActionCode> getActionCodes() {
		return actionCodes;
	}

	public void setActionCodes(List<ActionCode> actionCodes) {
		this.actionCodes = actionCodes;
	}

	public List<ResultCode> getResultCodes() {
		return resultCodes;
	}

	public void setResultCodes(List<ResultCode> resultCodes) {
		this.resultCodes = resultCodes;
	}

	public List<ResultCodeGroup> getResultCodeGroups() {
		return resultCodeGroups;
	}

	public void setResultCodeGroups(List<ResultCodeGroup> resultCodeGroups) {
		this.resultCodeGroups = resultCodeGroups;
	}

}
