package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.MenuType;

public class MenuTypeChildCriteriaResp extends CommonCriteriaResp {
	private List<MenuType> menuTypeChilds;
	
	public MenuTypeChildCriteriaResp() {}
	
	public MenuTypeChildCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<MenuType> getMenuTypeChilds() {
		return menuTypeChilds;
	}
	public void setMenuTypeChilds(List<MenuType> menuTypeChilds) {
		this.menuTypeChilds = menuTypeChilds;
	}

}
