package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.BankAccounts;

public class BankAccCriteriaResp extends CommonCriteriaResp {
	private List<BankAccounts> bankAccs;
	
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

}
