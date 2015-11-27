package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Menu;

public class MenuSearchCriteriaResp extends CommonCriteriaResp {
	private Long TotalItems;
	private List<Menu> menus;
	
	public MenuSearchCriteriaResp() {}
	
	public MenuSearchCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Menu> getMenus() {
		return menus;
	}
	public void setMenus(List<Menu> menus) {
		this.menus = menus;
	}
	public Long getTotalItems() {
		return TotalItems;
	}
	public void setTotalItems(Long totalItems) {
		TotalItems = totalItems;
	}

}
