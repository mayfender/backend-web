package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ResultCode;
import com.may.ple.backend.entity.ResultCodeGroup;

public class ResultCodeFindCriteriaResp extends CommonCriteriaResp {
	private List<ResultCode> resultCodes;
	private List<ResultCodeGroup> resultCodeGroups;
	
	public ResultCodeFindCriteriaResp() {}
	
	public ResultCodeFindCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
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