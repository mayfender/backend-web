package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ResultCodeGroup;

public class ResultCodeGroupFindCriteriaResp extends CommonCriteriaResp {
	private List<ResultCodeGroup> resultCodeGroups;
	
	public ResultCodeGroupFindCriteriaResp() {}
	
	public ResultCodeGroupFindCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<ResultCodeGroup> getResultCodeGroups() {
		return resultCodeGroups;
	}

	public void setResultCodeGroups(List<ResultCodeGroup> resultCodeGroups) {
		this.resultCodeGroups = resultCodeGroups;
	}

}
