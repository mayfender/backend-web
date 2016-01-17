package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.MenuType;

public class GetMenuTypeCriteriaResp extends CommonCriteriaResp {
	private Map<String, List<MenuType>> menuTypesMap;
	private Map<String, List<Menu>> menusMap;
	
	public GetMenuTypeCriteriaResp(){}
	
	public GetMenuTypeCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Map<String, List<MenuType>> getMenuTypesMap() {
		return menuTypesMap;
	}
	public void setMenuTypesMap(Map<String, List<MenuType>> menuTypesMap) {
		this.menuTypesMap = menuTypesMap;
	}
	public Map<String, List<Menu>> getMenusMap() {
		return menusMap;
	}
	public void setMenusMap(Map<String, List<Menu>> menusMap) {
		this.menusMap = menusMap;
	}
	
}
