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

}
