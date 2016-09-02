package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ApplicationSetting;

public class SettingDataCriteriaResp extends CommonCriteriaResp {
	private ApplicationSetting setting;
	
	public SettingDataCriteriaResp() {}
	
	public SettingDataCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public ApplicationSetting getSetting() {
		return setting;
	}

	public void setSetting(ApplicationSetting setting) {
		this.setting = setting;
	}

}
