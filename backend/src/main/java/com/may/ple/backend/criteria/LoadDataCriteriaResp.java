package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Menu;

public class LoadDataCriteriaResp extends CommonCriteriaResp {
	private Map<String, List<Menu>> menus;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Map<String, List<Menu>> getMenus() {
		return menus;
	}
	public void setMenus(Map<String, List<Menu>> menus) {
		this.menus = menus;
	}
	
}
