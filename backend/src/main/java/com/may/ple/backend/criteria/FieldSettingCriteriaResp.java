package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.FieldSetting;

public class FieldSettingCriteriaResp extends CommonCriteriaResp {
	private List<FieldSetting> fieldSettings;
	private String id;
	
	public FieldSettingCriteriaResp() {}
	
	public FieldSettingCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<FieldSetting> getFieldSettings() {
		return fieldSettings;
	}

	public void setFieldSettings(List<FieldSetting> fieldSettings) {
		this.fieldSettings = fieldSettings;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
