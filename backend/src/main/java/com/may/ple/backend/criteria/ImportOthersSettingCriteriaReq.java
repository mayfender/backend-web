package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ImportOthersSettingCriteriaReq {
	private String productId;
	private String idCardNoColumnName;
	private String contractNoColumnName;
	private String menuId;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getIdCardNoColumnName() {
		return idCardNoColumnName;
	}

	public void setIdCardNoColumnName(String idCardNoColumnName) {
		this.idCardNoColumnName = idCardNoColumnName;
	}

	public String getMenuId() {
		return menuId;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

	public String getContractNoColumnName() {
		return contractNoColumnName;
	}

	public void setContractNoColumnName(String contractNoColumnName) {
		this.contractNoColumnName = contractNoColumnName;
	}
	
}
