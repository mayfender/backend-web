package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.OrderName;

public class OrderCriteriaResp extends CommonCriteriaResp {
	private List<Map> periods;
	private List<Map> orderData;
	private List orderNameLst;
	private OrderName orderName;
	private Double totalPriceSum;
	private Double totalPriceSumAll;
	private String receiverId;
	private Map<String, Map<String, List<Map>>> chkResultMap;
	private Map<String, Double> totalPriceSumAllMap;
	private Map<String, Object> dataMap;
	private int movedNum;
	
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

	public List getOrderNameLst() {
		return orderNameLst;
	}

	public void setOrderNameLst(List orderNameLst) {
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

}
