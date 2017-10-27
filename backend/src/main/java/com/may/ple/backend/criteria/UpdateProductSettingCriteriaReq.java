package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class UpdateProductSettingCriteriaReq {
	private String productId;
	private String balanceColumnName;
	private String contractNoColumnName;
	private String idCardNoColumnName;
	private String expirationDateColumnName;
	private String birthDateColumnName;
	private String sortingColumnName;
	private String paidDateColumnName;
	private Boolean isPayment;
	
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

	public String getBalanceColumnName() {
		return balanceColumnName;
	}

	public void setBalanceColumnName(String balanceColumnName) {
		this.balanceColumnName = balanceColumnName;
	}

	public Boolean getIsPayment() {
		return isPayment;
	}

	public void setIsPayment(Boolean isPayment) {
		this.isPayment = isPayment;
	}

	public String getExpirationDateColumnName() {
		return expirationDateColumnName;
	}

	public void setExpirationDateColumnName(String expirationDateColumnName) {
		this.expirationDateColumnName = expirationDateColumnName;
	}

	public String getSortingColumnName() {
		return sortingColumnName;
	}

	public void setSortingColumnName(String sortingColumnName) {
		this.sortingColumnName = sortingColumnName;
	}

	public String getPaidDateColumnName() {
		return paidDateColumnName;
	}

	public void setPaidDateColumnName(String paidDateColumnName) {
		this.paidDateColumnName = paidDateColumnName;
	}

	public String getBirthDateColumnName() {
		return birthDateColumnName;
	}

	public void setBirthDateColumnName(String birthDateColumnName) {
		this.birthDateColumnName = birthDateColumnName;
	}
	
}
