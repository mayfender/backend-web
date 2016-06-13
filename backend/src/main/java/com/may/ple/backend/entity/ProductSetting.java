package com.may.ple.backend.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ProductSetting {
	private String balanceColumn;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getBalanceColumn() {
		return balanceColumn;
	}

	public void setBalanceColumn(String balanceColumn) {
		this.balanceColumn = balanceColumn;
	}

}
