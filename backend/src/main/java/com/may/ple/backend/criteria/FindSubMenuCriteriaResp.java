package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.SubMenu;

public class FindSubMenuCriteriaResp extends CommonCriteriaResp {
	private List<SubMenu> subMenus;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<SubMenu> getSubMenus() {
		return subMenus;
	}
	public void setSubMenus(List<SubMenu> subMenus) {
		this.subMenus = subMenus;
	}

}
