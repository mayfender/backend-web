package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.BankAccounts;

public class BankAccCriteriaResp extends CommonCriteriaResp {
	private List<BankAccounts> bankAccs;
	private String customerComInfo;
	private String customerAddress;
	private String customerEmail;
	
	public BankAccCriteriaResp() {}
	
	public BankAccCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<BankAccounts> getBankAccs() {
		return bankAccs;
	}

	public void setBankAccs(List<BankAccounts> bankAccs) {
		this.bankAccs = bankAccs;
	}

	public String getCustomerComInfo() {
		return customerComInfo;
	}

	public void setCustomerComInfo(String customerComInfo) {
		this.customerComInfo = customerComInfo;
	}

	public String getCustomerAddress() {
		return customerAddress;
	}

	public void setCustomerAddress(String customerAddress) {
		this.customerAddress = customerAddress;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

}