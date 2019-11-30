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
	private Integer showUploadDoc;
	private Integer seizure;
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
	private Integer updateType;
	private Boolean isDisableNoticePrint;
	private Boolean isHideComment;
	private Boolean isHideDashboard;
	private Boolean isHideAlert;
	private Boolean isDisableBtnShow;
	private String userKYSLaw;
	private String passKYSLaw;
	private String userKYS;
	private String passKYS;
	private String userKRO;
	private String passKRO;
	private Integer privateChatDisabled;
	private Integer updateEmptyReminderDate;
	private List<Map> smsMessages;
	private Boolean isSmsEnable;
	private String smsUsername;
	private String smsPassword;
	private String smsSenderName;
	private Integer dsf;
	private Map receipt;
	
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

	public Integer getUpdateType() {
		return updateType;
	}

	public void setUpdateType(Integer updateType) {
		this.updateType = updateType;
	}

	public Boolean getIsDisableNoticePrint() {
		return isDisableNoticePrint;
	}

	public void setIsDisableNoticePrint(Boolean isDisableNoticePrint) {
		this.isDisableNoticePrint = isDisableNoticePrint;
	}

	public Boolean getIsHideComment() {
		return isHideComment;
	}

	public void setIsHideComment(Boolean isHideComment) {
		this.isHideComment = isHideComment;
	}

	public Boolean getIsHideDashboard() {
		return isHideDashboard;
	}

	public void setIsHideDashboard(Boolean isHideDashboard) {
		this.isHideDashboard = isHideDashboard;
	}

	public Integer getShowUploadDoc() {
		return showUploadDoc;
	}

	public void setShowUploadDoc(Integer showUploadDoc) {
		this.showUploadDoc = showUploadDoc;
	}

	public Boolean getIsHideAlert() {
		return isHideAlert;
	}

	public void setIsHideAlert(Boolean isHideAlert) {
		this.isHideAlert = isHideAlert;
	}

	public Boolean getIsDisableBtnShow() {
		return isDisableBtnShow;
	}

	public void setIsDisableBtnShow(Boolean isDisableBtnShow) {
		this.isDisableBtnShow = isDisableBtnShow;
	}

	public String getUserKYSLaw() {
		return userKYSLaw;
	}

	public void setUserKYSLaw(String userKYSLaw) {
		this.userKYSLaw = userKYSLaw;
	}

	public String getPassKYSLaw() {
		return passKYSLaw;
	}

	public void setPassKYSLaw(String passKYSLaw) {
		this.passKYSLaw = passKYSLaw;
	}

	public String getUserKYS() {
		return userKYS;
	}

	public void setUserKYS(String userKYS) {
		this.userKYS = userKYS;
	}

	public String getPassKYS() {
		return passKYS;
	}

	public void setPassKYS(String passKYS) {
		this.passKYS = passKYS;
	}

	public String getUserKRO() {
		return userKRO;
	}

	public void setUserKRO(String userKRO) {
		this.userKRO = userKRO;
	}

	public String getPassKRO() {
		return passKRO;
	}

	public void setPassKRO(String passKRO) {
		this.passKRO = passKRO;
	}

	public Integer getSeizure() {
		return seizure;
	}

	public void setSeizure(Integer seizure) {
		this.seizure = seizure;
	}

	public Integer getPrivateChatDisabled() {
		return privateChatDisabled;
	}

	public void setPrivateChatDisabled(Integer privateChatDisabled) {
		this.privateChatDisabled = privateChatDisabled;
	}

	public Integer getUpdateEmptyReminderDate() {
		return updateEmptyReminderDate;
	}

	public void setUpdateEmptyReminderDate(Integer updateEmptyReminderDate) {
		this.updateEmptyReminderDate = updateEmptyReminderDate;
	}

	public List<Map> getSmsMessages() {
		return smsMessages;
	}

	public void setSmsMessages(List<Map> smsMessages) {
		this.smsMessages = smsMessages;
	}

	public Boolean getIsSmsEnable() {
		return isSmsEnable;
	}

	public void setIsSmsEnable(Boolean isSmsEnable) {
		this.isSmsEnable = isSmsEnable;
	}

	public String getSmsUsername() {
		return smsUsername;
	}

	public void setSmsUsername(String smsUsername) {
		this.smsUsername = smsUsername;
	}

	public String getSmsPassword() {
		return smsPassword;
	}

	public void setSmsPassword(String smsPassword) {
		this.smsPassword = smsPassword;
	}

	public String getSmsSenderName() {
		return smsSenderName;
	}

	public void setSmsSenderName(String smsSenderName) {
		this.smsSenderName = smsSenderName;
	}

	public Integer getDsf() {
		return dsf;
	}

	public void setDsf(Integer dsf) {
		this.dsf = dsf;
	}

	public Map getReceipt() {
		return receipt;
	}

	public void setReceipt(Map receipt) {
		this.receipt = receipt;
	}

}
