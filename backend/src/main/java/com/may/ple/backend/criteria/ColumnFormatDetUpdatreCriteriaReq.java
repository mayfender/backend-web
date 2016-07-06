package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.model.ColumnFormatGroup;

public class ColumnFormatDetUpdatreCriteriaReq {
	private List<ColumnFormatGroup> colFormGroups;
	private String productId;
	private String menuId;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<ColumnFormatGroup> getColFormGroups() {
		return colFormGroups;
	}

	public void setColFormGroups(List<ColumnFormatGroup> colFormGroups) {
		this.colFormGroups = colFormGroups;
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
