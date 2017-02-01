package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Database;

public class PersistProductCriteriaReq {
	private String id;
	private String productName;
	private Integer enabled;
	private Database database;
	private List<ColumnFormat> columnFormats;
	private String columnName;
	private Boolean isActive;
	private Boolean isPayment;
	private Boolean isTraceExportExcel;
	private Boolean isTraceExportTxt;
	private Integer traceDateRoundDay;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Integer getEnabled() {
		return enabled;
	}

	public void setEnabled(Integer enabled) {
		this.enabled = enabled;
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public List<ColumnFormat> getColumnFormats() {
		return columnFormats;
	}

	public void setColumnFormats(List<ColumnFormat> columnFormats) {
		this.columnFormats = columnFormats;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Boolean getIsPayment() {
		return isPayment;
	}

	public void setIsPayment(Boolean isPayment) {
		this.isPayment = isPayment;
	}

	public Boolean getIsTraceExportExcel() {
		return isTraceExportExcel;
	}

	public void setIsTraceExportExcel(Boolean isTraceExportExcel) {
		this.isTraceExportExcel = isTraceExportExcel;
	}

	public Boolean getIsTraceExportTxt() {
		return isTraceExportTxt;
	}

	public void setIsTraceExportTxt(Boolean isTraceExportTxt) {
		this.isTraceExportTxt = isTraceExportTxt;
	}

	public Integer getTraceDateRoundDay() {
		return traceDateRoundDay;
	}

	public void setTraceDateRoundDay(Integer traceDateRoundDay) {
		this.traceDateRoundDay = traceDateRoundDay;
	}

}
