package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;

public class GetColumnFormatsCriteriaResp extends CommonCriteriaResp {
	private List<ColumnFormat> columnFormats;
	private List<ColumnFormat> mainColumnFormats;
	private String contractNoColumnName;
	private String idCardNoColumnName;
	private String balanceColumnName;
	private String expirationDateColumnName;
	private String birthDateColumnName;
	private String sortingColumnName;
	private String paidDateColumnName;
	
	public GetColumnFormatsCriteriaResp(){}
	
	public GetColumnFormatsCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<ColumnFormat> getColumnFormats() {
		return columnFormats;
	}

	public void setColumnFormats(List<ColumnFormat> columnFormats) {
		this.columnFormats = columnFormats;
	}

	public List<ColumnFormat> getMainColumnFormats() {
		return mainColumnFormats;
	}

	public void setMainColumnFormats(List<ColumnFormat> mainColumnFormats) {
		this.mainColumnFormats = mainColumnFormats;
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
