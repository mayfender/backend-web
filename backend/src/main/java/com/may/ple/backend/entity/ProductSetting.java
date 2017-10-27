package com.may.ple.backend.entity;

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

}
