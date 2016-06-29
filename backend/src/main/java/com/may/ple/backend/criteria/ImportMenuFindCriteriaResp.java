package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ImportMenu;

public class ImportMenuFindCriteriaResp extends CommonCriteriaResp {
	private List<ImportMenu> menus;
	
	public ImportMenuFindCriteriaResp(){}
	
	public ImportMenuFindCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<ImportMenu> getMenus() {
		return menus;
	}

	public void setMenus(List<ImportMenu> menus) {
		this.menus = menus;
	}

}
