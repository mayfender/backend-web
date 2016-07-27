package com.may.ple.backend.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ProductSetting {
	private String balanceColumn;
	private String contractNoColumnName;
	private String idCardNoColumnName;
	
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

	public String getContractNoColumnName() {
		return contractNoColumnName;
	}

	public void setContractNoColumnName(String contractNoColumnName) {
		this.contractNoColumnName = contractNoColumnName;
	}

	public String getIdCardNoColumnName() {
		return idCardNoColumnName;
	}

	public void setIdCardNoColumnName(String idCardNoColumnName) {
		this.idCardNoColumnName = idCardNoColumnName;
	}

}
