package com.may.ple.backend.criteria;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.OrderName;
import com.may.ple.backend.entity.Receiver;
import com.may.ple.backend.entity.Users;

public class OrderCriteriaResp extends CommonCriteriaResp {
	private List<Map> periods;
	private List<Map> orderData;
	private List<String> orderNameLst;
	private OrderName orderName;
	private Double totalPriceSum;
	private Double totalPriceSumAll;
	private String receiverId;
	private Map<String, Map<String, List<Map>>> chkResultMap;
	private List<Map> chkResultList;
	private Map<String, Double> totalPriceSumAllMap;
	private Map<String, Object> dataMap;
	private int movedNum;
	private List<Receiver> receiverList;
	private Map restrictedOrder;
	private List<Users> users;
	private List createdDateGroup;
	private Date createdDateTime;
	private Date sendRoundDateTime;
	private String sendRoundMsg;
	private Map<String, Integer> restrictList;
	private Boolean notAllowRemove;
	private Boolean isOverOrderTime;
	private Map<String, Object> paymentData;
	private List<String> orderNumberList;
	private List<Map> pinNums;
	private String errorMsg;
	private Map<String, Object> orderFile;
	private Boolean isEmpty;

	public OrderCriteriaResp() {}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public OrderCriteriaResp(int statusCode) {
		super(statusCode);
	}

	public List<Map> getPeriods() {
		return periods;
	}

	public void setPeriods(List<Map> periods) {
		this.periods = periods;
	}

	public List<Map> getOrderData() {
		return orderData;
	}

	public void setOrderData(List<Map> orderData) {
		this.orderData = orderData;
	}

	public OrderName getOrderName() {
		return orderName;
	}

	public void setOrderName(OrderName orderName) {
		this.orderName = orderName;
	}

	public List<String> getOrderNameLst() {
		return orderNameLst;
	}

	public void setOrderNameLst(List<String> orderNameLst) {
		this.orderNameLst = orderNameLst;
	}

	public Double getTotalPriceSum() {
		return totalPriceSum;
	}

	public void setTotalPriceSum(Double totalPriceSum) {
		this.totalPriceSum = totalPriceSum;
	}

	public Double getTotalPriceSumAll() {
		return totalPriceSumAll;
	}

	public void setTotalPriceSumAll(Double totalPriceSumAll) {
		this.totalPriceSumAll = totalPriceSumAll;
	}

	public String getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}

	public Map<String, Double> getTotalPriceSumAllMap() {
		return totalPriceSumAllMap;
	}

	public void setTotalPriceSumAllMap(Map<String, Double> totalPriceSumAllMap) {
		this.totalPriceSumAllMap = totalPriceSumAllMap;
	}

	public Map<String, Map<String, List<Map>>> getChkResultMap() {
		return chkResultMap;
	}

	public void setChkResultMap(Map<String, Map<String, List<Map>>> chkResultMap) {
		this.chkResultMap = chkResultMap;
	}

	public Map<String, Object> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, Object> dataMap) {
		this.dataMap = dataMap;
	}

	public int getMovedNum() {
		return movedNum;
	}

	public void setMovedNum(int movedNum) {
		this.movedNum = movedNum;
	}

	public List<Receiver> getReceiverList() {
		return receiverList;
	}

	public void setReceiverList(List<Receiver> receiverList) {
		this.receiverList = receiverList;
	}

	public Map getRestrictedOrder() {
		return restrictedOrder;
	}

	public void setRestrictedOrder(Map restrictedOrder) {
		this.restrictedOrder = restrictedOrder;
	}

	public List<Users> getUsers() {
		return users;
	}

	public void setUsers(List<Users> users) {
		this.users = users;
	}

	public List<Map> getChkResultList() {
		return chkResultList;
	}

	public void setChkResultList(List<Map> chkResultList) {
		this.chkResultList = chkResultList;
	}

	public List getCreatedDateGroup() {
		return createdDateGroup;
	}

	public void setCreatedDateGroup(List createdDateGroup) {
		this.createdDateGroup = createdDateGroup;
	}

	public Date getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public Map<String, Integer> getRestrictList() {
		return restrictList;
	}

	public void setRestrictList(Map<String, Integer> restrictList) {
		this.restrictList = restrictList;
	}

	public Date getSendRoundDateTime() {
		return sendRoundDateTime;
	}

	public void setSendRoundDateTime(Date sendRoundDateTime) {
		this.sendRoundDateTime = sendRoundDateTime;
	}

	public String getSendRoundMsg() {
		return sendRoundMsg;
	}

	public void setSendRoundMsg(String sendRoundMsg) {
		this.sendRoundMsg = sendRoundMsg;
	}

	public Boolean getNotAllowRemove() {
		return notAllowRemove;
	}

	public void setNotAllowRemove(Boolean notAllowRemove) {
		this.notAllowRemove = notAllowRemove;
	}

	public Boolean getIsOverOrderTime() {
		return isOverOrderTime;
	}

	public void setIsOverOrderTime(Boolean isOverOrderTime) {
		this.isOverOrderTime = isOverOrderTime;
	}

	public Map<String, Object> getPaymentData() {
		return paymentData;
	}

	public void setPaymentData(Map<String, Object> paymentData) {
		this.paymentData = paymentData;
	}

	public List<String> getOrderNumberList() {
		return orderNumberList;
	}

	public void setOrderNumberList(List<String> orderNumberList) {
		this.orderNumberList = orderNumberList;
	}

	public List<Map> getPinNums() {
		return pinNums;
	}

	public void setPinNums(List<Map> pinNums) {
		this.pinNums = pinNums;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Map<String, Object> getOrderFile() {
		return orderFile;
	}

	public void setOrderFile(Map<String, Object> orderFile) {
		this.orderFile = orderFile;
	}

	public Boolean getIsEmpty() {
		return isEmpty;
	}

	public void setIsEmpty(Boolean isEmpty) {
		this.isEmpty = isEmpty;
	}

}
