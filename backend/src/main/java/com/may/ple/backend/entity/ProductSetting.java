package com.may.ple.backend.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ProductSetting {
	private String balanceColumnName;
	private String contractNoColumnName;
	private String idCardNoColumnName;
	private String contractNoColumnNamePayment;
	private String idCardNoColumnNamePayment;
	private String expirationDateColumnName;
	private Integer startTimeH;
	private Integer startTimeM;
	private Integer endTimeH;
	private Integer endTimeM;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
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

	public String getContractNoColumnNamePayment() {
		return contractNoColumnNamePayment;
	}

	public void setContractNoColumnNamePayment(String contractNoColumnNamePayment) {
		this.contractNoColumnNamePayment = contractNoColumnNamePayment;
	}

	public String getIdCardNoColumnNamePayment() {
		return idCardNoColumnNamePayment;
	}

	public void setIdCardNoColumnNamePayment(String idCardNoColumnNamePayment) {
		this.idCardNoColumnNamePayment = idCardNoColumnNamePayment;
	}

	public String getExpirationDateColumnName() {
		return expirationDateColumnName;
	}

	public void setExpirationDateColumnName(String expirationDateColumnName) {
		this.expirationDateColumnName = expirationDateColumnName;
	}

	public Integer getStartTimeH() {
		return startTimeH;
	}

	public void setStartTimeH(Integer startTimeH) {
		this.startTimeH = startTimeH;
	}

	public Integer getStartTimeM() {
		return startTimeM;
	}

	public void setStartTimeM(Integer startTimeM) {
		this.startTimeM = startTimeM;
	}

	public Integer getEndTimeH() {
		return endTimeH;
	}

	public void setEndTimeH(Integer endTimeH) {
		this.endTimeH = endTimeH;
	}

	public Integer getEndTimeM() {
		return endTimeM;
	}

	public void setEndTimeM(Integer endTimeM) {
		this.endTimeM = endTimeM;
	}

}
