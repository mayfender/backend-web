package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.MenuType;

public class MenuTypeLoadCriteriaResp extends CommonCriteriaResp {
	private List<MenuType> menuTypes;
	
	public MenuTypeLoadCriteriaResp() {}
	
	public MenuTypeLoadCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<MenuType> getMenuTypes() {
		return menuTypes;
	}
	public void setMenuTypes(List<MenuType> menuTypes) {
		this.menuTypes = menuTypes;
	}

}
