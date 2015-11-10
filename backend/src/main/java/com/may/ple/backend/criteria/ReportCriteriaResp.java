package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ReportCriteriaResp extends CommonCriteriaResp {
	private Map<String, List<String>> result;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Map<String, List<String>> getResult() {
		return result;
	}

	public void setResult(Map<String, List<String>> result) {
		this.result = result;
	}

}
