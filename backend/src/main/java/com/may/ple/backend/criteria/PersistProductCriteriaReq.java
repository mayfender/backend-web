package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Database;

public class PersistProductCriteriaReq {
	private String id;
	private String productName;
	private Integer enabled;
	private Integer pocModule;
	private Integer createdByLog;
	private Integer userEditable;
	private Integer autoUpdateBalance;
	private Integer noticeFramework;
	private Database database;
	private List<ColumnFormat> columnFormats;
	private String columnName;
	private Boolean isActive;
	private Boolean isPayment;
	private Boolean isTraceExportExcel;
	private Boolean isTraceExportTxt;
	private Integer traceDateRoundDay;
	private String paymentRules;
	private String discountColumnName;
	private Integer textLength;
	private List<Map> discountFields;
	private List<Map> payTypes;
	private String openOfficeHost;
	private Integer openOfficePort;
	
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

	public Integer getNoticeFramework() {
		return noticeFramework;
	}

	public void setNoticeFramework(Integer noticeFramework) {
		this.noticeFramework = noticeFramework;
	}

	public Integer getPocModule() {
		return pocModule;
	}

	public void setPocModule(Integer pocModule) {
		this.pocModule = pocModule;
	}

	public Integer getAutoUpdateBalance() {
		return autoUpdateBalance;
	}

	public void setAutoUpdateBalance(Integer autoUpdateBalance) {
		this.autoUpdateBalance = autoUpdateBalance;
	}

	public String getPaymentRules() {
		return paymentRules;
	}

	public void setPaymentRules(String paymentRules) {
		this.paymentRules = paymentRules;
	}
	public Integer getCreatedByLog() {
		return createdByLog;
	}

	public void setCreatedByLog(Integer createdByLog) {
		this.createdByLog = createdByLog;
	}

	public String getDiscountColumnName() {
		return discountColumnName;
	}

	public void setDiscountColumnName(String discountColumnName) {
		this.discountColumnName = discountColumnName;
	}

	public Integer getTextLength() {
		return textLength;
	}

	public void setTextLength(Integer textLength) {
		this.textLength = textLength;
	}

	public List<Map> getDiscountFields() {
		return discountFields;
	}

	public void setDiscountFields(List<Map> discountFields) {
		this.discountFields = discountFields;
	}

	public Integer getUserEditable() {
		return userEditable;
	}

	public void setUserEditable(Integer userEditable) {
		this.userEditable = userEditable;
	}

	public List<Map> getPayTypes() {
		return payTypes;
	}

	public void setPayTypes(List<Map> payTypes) {
		this.payTypes = payTypes;
	}

	public String getOpenOfficeHost() {
		return openOfficeHost;
	}

	public void setOpenOfficeHost(String openOfficeHost) {
		this.openOfficeHost = openOfficeHost;
	}

	public Integer getOpenOfficePort() {
		return openOfficePort;
	}

	public void setOpenOfficePort(Integer openOfficePort) {
		this.openOfficePort = openOfficePort;
	}

}
