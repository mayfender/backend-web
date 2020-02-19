package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class LpsCriteriaResp extends CommonCriteriaResp {
	private Map<String, Map> lpsList;
	private List fields;
	private String lpsTel;
	
	public LpsCriteriaResp() {}
	
	public LpsCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Map<String, Map> getLpsList() {
		return lpsList;
	}

	public void setLpsList(Map<String, Map> lpsList) {
		this.lpsList = lpsList;
	}

	public List getFields() {
		return fields;
	}

	public void setFields(List fields) {
		this.fields = fields;
	}

	public String getLpsTel() {
		return lpsTel;
	}

	public void setLpsTel(String lpsTel) {
		this.lpsTel = lpsTel;
	}

}
