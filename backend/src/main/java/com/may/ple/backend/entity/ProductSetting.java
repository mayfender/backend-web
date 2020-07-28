package com.may.ple.backend.entity;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ProductSetting {
	private String balanceColumnName;
	private String contractNoColumnName;
	private String idCardNoColumnName;
	private String contractNoColumnNamePayment;
	private String idCardNoColumnNamePayment;
	private String sortingColumnNamePayment;
	private String paidDateColumnNamePayment;
	private String expirationDateColumnName;
	private String birthDateColumnName;
	private String discountColumnName;
	private Integer normalStartTimeH;
	private Integer normalStartTimeM;
	private Integer normalEndTimeH;
	private Integer normalEndTimeM;
	private Integer satStartTimeH;
	private Integer satStartTimeM;
	private Integer satEndTimeH;
	private Integer satEndTimeM;
	private Integer sunStartTimeH;
	private Integer sunStartTimeM;
	private Integer sunEndTimeH;
	private Integer sunEndTimeM;
	private Boolean normalWorkingDayEnable;
	private Boolean satWorkingDayEnable;
	private Boolean sunWorkingDayEnable;
	private Boolean isDisableNoticePrint = Boolean.FALSE;
	private Boolean isHideComment = Boolean.FALSE;
	private Boolean isTraceExportExcel;
	private Boolean isTraceExportTxt;
	private Integer traceDateRoundDay;
	private Integer noticeFramework;
	private Integer pocModule;
	private Integer createdByLog;
	private Integer userEditable;
	private Integer showUploadDoc;
	private Integer seizure;
	private Integer autoUpdateBalance;
	private String paymentRules;
	private Integer textLength;
	private List<Map> discountFields;
	private List<Map> payTypes;
	private String openOfficeHost;
	private Integer openOfficePort;
	private Boolean isHideDashboard = Boolean.FALSE;
	private Boolean isHideAlert = Boolean.FALSE;
	private Boolean isDisableBtnShow = Boolean.FALSE;
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
	private Map receipt;
	private Map lps;

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

	public Integer getNormalStartTimeH() {
		return normalStartTimeH;
	}

	public void setNormalStartTimeH(Integer normalStartTimeH) {
		this.normalStartTimeH = normalStartTimeH;
	}

	public Integer getNormalStartTimeM() {
		return normalStartTimeM;
	}

	public void setNormalStartTimeM(Integer normalStartTimeM) {
		this.normalStartTimeM = normalStartTimeM;
	}

	public Integer getNormalEndTimeH() {
		return normalEndTimeH;
	}

	public void setNormalEndTimeH(Integer normalEndTimeH) {
		this.normalEndTimeH = normalEndTimeH;
	}

	public Integer getNormalEndTimeM() {
		return normalEndTimeM;
	}

	public void setNormalEndTimeM(Integer normalEndTimeM) {
		this.normalEndTimeM = normalEndTimeM;
	}

	public Integer getSatStartTimeH() {
		return satStartTimeH;
	}

	public void setSatStartTimeH(Integer satStartTimeH) {
		this.satStartTimeH = satStartTimeH;
	}

	public Integer getSatStartTimeM() {
		return satStartTimeM;
	}

	public void setSatStartTimeM(Integer satStartTimeM) {
		this.satStartTimeM = satStartTimeM;
	}

	public Integer getSatEndTimeH() {
		return satEndTimeH;
	}

	public void setSatEndTimeH(Integer satEndTimeH) {
		this.satEndTimeH = satEndTimeH;
	}

	public Integer getSatEndTimeM() {
		return satEndTimeM;
	}

	public void setSatEndTimeM(Integer satEndTimeM) {
		this.satEndTimeM = satEndTimeM;
	}

	public Integer getSunStartTimeH() {
		return sunStartTimeH;
	}

	public void setSunStartTimeH(Integer sunStartTimeH) {
		this.sunStartTimeH = sunStartTimeH;
	}

	public Integer getSunStartTimeM() {
		return sunStartTimeM;
	}

	public void setSunStartTimeM(Integer sunStartTimeM) {
		this.sunStartTimeM = sunStartTimeM;
	}

	public Integer getSunEndTimeH() {
		return sunEndTimeH;
	}

	public void setSunEndTimeH(Integer sunEndTimeH) {
		this.sunEndTimeH = sunEndTimeH;
	}

	public Integer getSunEndTimeM() {
		return sunEndTimeM;
	}

	public void setSunEndTimeM(Integer sunEndTimeM) {
		this.sunEndTimeM = sunEndTimeM;
	}

	public Boolean getNormalWorkingDayEnable() {
		return normalWorkingDayEnable;
	}

	public void setNormalWorkingDayEnable(Boolean normalWorkingDayEnable) {
		this.normalWorkingDayEnable = normalWorkingDayEnable;
	}

	public Boolean getSatWorkingDayEnable() {
		return satWorkingDayEnable;
	}

	public void setSatWorkingDayEnable(Boolean satWorkingDayEnable) {
		this.satWorkingDayEnable = satWorkingDayEnable;
	}

	public Boolean getSunWorkingDayEnable() {
		return sunWorkingDayEnable;
	}

	public void setSunWorkingDayEnable(Boolean sunWorkingDayEnable) {
		this.sunWorkingDayEnable = sunWorkingDayEnable;
	}

	public Boolean getIsDisableNoticePrint() {
		return isDisableNoticePrint;
	}

	public void setIsDisableNoticePrint(Boolean isDisableNoticePrint) {
		this.isDisableNoticePrint = isDisableNoticePrint;
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

	public String getSortingColumnNamePayment() {
		return sortingColumnNamePayment;
	}

	public void setSortingColumnNamePayment(String sortingColumnNamePayment) {
		this.sortingColumnNamePayment = sortingColumnNamePayment;
	}

	public String getPaidDateColumnNamePayment() {
		return paidDateColumnNamePayment;
	}

	public void setPaidDateColumnNamePayment(String paidDateColumnNamePayment) {
		this.paidDateColumnNamePayment = paidDateColumnNamePayment;
	}

	public Integer getNoticeFramework() {
		return noticeFramework;
	}

	public void setNoticeFramework(Integer noticeFramework) {
		this.noticeFramework = noticeFramework;
	}

	public Boolean getIsHideComment() {
		return isHideComment;
	}

	public void setIsHideComment(Boolean isHideComment) {
		this.isHideComment = isHideComment;
	}

	public Integer getPocModule() {
		return pocModule;
	}

	public void setPocModule(Integer pocModule) {
		this.pocModule = pocModule;
	}

	public String getBirthDateColumnName() {
		return birthDateColumnName;
	}

	public void setBirthDateColumnName(String birthDateColumnName) {
		this.birthDateColumnName = birthDateColumnName;
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

	public Map getReceipt() {
		return receipt;
	}

	public void setReceipt(Map receipt) {
		this.receipt = receipt;
	}

	public Map getLps() {
		return lps;
	}

	public void setLps(Map lps) {
		this.lps = lps;
	}

}
