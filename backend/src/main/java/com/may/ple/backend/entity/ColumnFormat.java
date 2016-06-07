package com.may.ple.backend.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ColumnFormat {
	private String columnName;
	private String columnNameAlias;
	private String dataType;
	private Boolean isActive;
	
	public ColumnFormat() {}
	
	public ColumnFormat(String columnName, Boolean isActive) {
		this.columnName = columnName;
		this.isActive = isActive;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnNameAlias() {
		return columnNameAlias;
	}

	public void setColumnNameAlias(String columnNameAlias) {
		this.columnNameAlias = columnNameAlias;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

}
