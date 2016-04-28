package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.dto.ReportMenuDto;

public class ReportMenuCriteriaResp extends CommonCriteriaResp {
	private List<ReportMenuDto> menus;
	
	public ReportMenuCriteriaResp() {}
	
	public ReportMenuCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<ReportMenuDto> getMenus() {
		return menus;
	}
	public void setMenus(List<ReportMenuDto> menus) {
		this.menus = menus;
	}

}
