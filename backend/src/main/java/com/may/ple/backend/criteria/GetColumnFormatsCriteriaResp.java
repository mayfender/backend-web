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
	
}
