package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ColumnLinkUpdateCriteriaReq {
	private String mainColumn;
	private String childColumn;
	private String productId;
	private String menuId;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getMainColumn() {
		return mainColumn;
	}

	public void setMainColumn(String mainColumn) {
		this.mainColumn = mainColumn;
	}

	public String getChildColumn() {
		return childColumn;
	}

	public void setChildColumn(String childColumn) {
		this.childColumn = childColumn;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getMenuId() {
		return menuId;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

}
