package com.may.ple.backend.criteria;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class OrderCriteriaReq {
	private Date periodDateTime;
	private String userId;
	private String periodId;
	private String name;
	private String orderNumber;
	private Double bon;
	private Boolean bonSw;
	private Double lang;
	private Boolean langSw;
	private Double tod;
	private Double loy;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Date getPeriodDateTime() {
		return periodDateTime;
	}

	public void setPeriodDateTime(Date periodDateTime) {
		this.periodDateTime = periodDateTime;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPeriodId() {
		return periodId;
	}

	public void setPeriodId(String periodId) {
		this.periodId = periodId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public Double getBon() {
		return bon;
	}

	public void setBon(Double bon) {
		this.bon = bon;
	}

	public Boolean getBonSw() {
		return bonSw;
	}

	public void setBonSw(Boolean bonSw) {
		this.bonSw = bonSw;
	}

	public Double getLang() {
		return lang;
	}

	public void setLang(Double lang) {
		this.lang = lang;
	}

	public Boolean getLangSw() {
		return langSw;
	}

	public void setLangSw(Boolean langSw) {
		this.langSw = langSw;
	}

	public Double getTod() {
		return tod;
	}

	public void setTod(Double tod) {
		this.tod = tod;
	}

	public Double getLoy() {
		return loy;
	}

	public void setLoy(Double loy) {
		this.loy = loy;
	}

}
