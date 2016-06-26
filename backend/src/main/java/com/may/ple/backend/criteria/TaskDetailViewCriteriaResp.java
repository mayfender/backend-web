package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;

public class TaskDetailViewCriteriaResp extends CommonCriteriaResp {
	private Map taskDetail;
	private List<ColumnFormat> fieldName;
	
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

	public List<ColumnFormat> getFieldName() {
		return fieldName;
	}

	public void setFieldName(List<ColumnFormat> fieldName) {
		this.fieldName = fieldName;
	}

}
