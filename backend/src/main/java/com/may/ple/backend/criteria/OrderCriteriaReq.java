package com.may.ple.backend.criteria;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.model.CheckBoxType;

public class OrderCriteriaReq {
	private Date periodDateTime;
	private String dealerId;
	private String userId;
	private String periodId;
	private Date periodDate;
	private String name;
	private String orderNumber;
	private Double bon;
	private Boolean bonSw;
	private Double lang;
	private Boolean langSw;
	private Double tod;
	private Double loy;
	private Double runBon;
	private Double runLang;
	private String tab;
	private String orderName;
	private String orderNameUpdate;
	private String result2;
	private String result3;
	private CheckBoxType chkBoxType;
	private String receiverId;
	private String orderId;
	private String operator;
	private Double price;
	private String moveFromId;
	private String moveToId;
	private List<String> receiverIds;
	private List<Map> noPriceOrds;
	private List<Map> halfPriceOrds;
	private List<Integer> typeLst;

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

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

	public String getOrderName() {
		return orderName;
	}

	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}

	public String getResult2() {
		return result2;
	}

	public void setResult2(String result2) {
		this.result2 = result2;
	}

	public String getResult3() {
		return result3;
	}

	public void setResult3(String result3) {
		this.result3 = result3;
	}

	public Date getPeriodDate() {
		return periodDate;
	}

	public void setPeriodDate(Date periodDate) {
		this.periodDate = periodDate;
	}

	public CheckBoxType getChkBoxType() {
		return chkBoxType;
	}

	public void setChkBoxType(CheckBoxType chkBoxType) {
		this.chkBoxType = chkBoxType;
	}

	public String getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getMoveFromId() {
		return moveFromId;
	}

	public void setMoveFromId(String moveFromId) {
		this.moveFromId = moveFromId;
	}

	public String getMoveToId() {
		return moveToId;
	}

	public void setMoveToId(String moveToId) {
		this.moveToId = moveToId;
	}

	public List<String> getReceiverIds() {
		return receiverIds;
	}

	public void setReceiverIds(List<String> receiverIds) {
		this.receiverIds = receiverIds;
	}

	public String getOrderNameUpdate() {
		return orderNameUpdate;
	}

	public void setOrderNameUpdate(String orderNameUpdate) {
		this.orderNameUpdate = orderNameUpdate;
	}

	public List<Map> getNoPriceOrds() {
		return noPriceOrds;
	}

	public void setNoPriceOrds(List<Map> noPriceOrds) {
		this.noPriceOrds = noPriceOrds;
	}

	public List<Map> getHalfPriceOrds() {
		return halfPriceOrds;
	}

	public void setHalfPriceOrds(List<Map> halfPriceOrds) {
		this.halfPriceOrds = halfPriceOrds;
	}

	public Double getRunBon() {
		return runBon;
	}

	public void setRunBon(Double runBon) {
		this.runBon = runBon;
	}

	public Double getRunLang() {
		return runLang;
	}

	public void setRunLang(Double runLang) {
		this.runLang = runLang;
	}

	public List<Integer> getTypeLst() {
		return typeLst;
	}

	public void setTypeLst(List<Integer> typeLst) {
		this.typeLst = typeLst;
	}

	public String getDealerId() {
		return dealerId;
	}

	public void setDealerId(String dealerId) {
		this.dealerId = dealerId;
	}

}
