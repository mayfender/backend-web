package com.may.ple.backend.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ColumnFormat {
	private String columnName;
	private String columnNameAlias;
	private String dataType;
	private Boolean isActive;
	private Boolean isSum;
	private Boolean isNotice;
	private Integer detGroupId;
	private Integer detOrder;
	private Boolean detIsActive;
	
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

	public Boolean getDetIsActive() {
		return detIsActive;
	}

	public void setDetIsActive(Boolean detIsActive) {
		this.detIsActive = detIsActive;
	}

	public Integer getDetGroupId() {
		return detGroupId;
	}

	public void setDetGroupId(Integer detGroupId) {
		this.detGroupId = detGroupId;
	}

	public Integer getDetOrder() {
		return detOrder;
	}

	public void setDetOrder(Integer detOrder) {
		this.detOrder = detOrder;
	}

	public Boolean getIsNotice() {
		return isNotice;
	}

	public void setIsNotice(Boolean isNotice) {
		this.isNotice = isNotice;
	}

	public Boolean getIsSum() {
		return isSum;
	}

	public void setIsSum(Boolean isSum) {
		this.isSum = isSum;
	}

}
