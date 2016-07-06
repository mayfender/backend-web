package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;

public class ColumnFormatDetActiveUpdateCriteriaReq {
	private ColumnFormat columnFormat;
	private String productId;
	private String menuId;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public ColumnFormat getColumnFormat() {
		return columnFormat;
	}

	public void setColumnFormat(ColumnFormat columnFormat) {
		this.columnFormat = columnFormat;
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
