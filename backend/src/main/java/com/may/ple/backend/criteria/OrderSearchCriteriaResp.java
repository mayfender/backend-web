package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.OrderMenu;

public class OrderSearchCriteriaResp extends CommonCriteriaResp {
	private List<OrderMenu> orders;
	private List<OrderMenu> ordersStart;
	private List<OrderMenu> ordersDoing;
	private List<OrderMenu> ordersFinished;
	private Double totalPrice;
	
	public OrderSearchCriteriaResp() {}
	
	public OrderSearchCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Double getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}
	public List<OrderMenu> getOrdersStart() {
		return ordersStart;
	}
	public void setOrdersStart(List<OrderMenu> ordersStart) {
		this.ordersStart = ordersStart;
	}
	public List<OrderMenu> getOrdersDoing() {
		return ordersDoing;
	}
	public void setOrdersDoing(List<OrderMenu> ordersDoing) {
		this.ordersDoing = ordersDoing;
	}
	public List<OrderMenu> getOrdersFinished() {
		return ordersFinished;
	}
	public void setOrdersFinished(List<OrderMenu> ordersFinished) {
		this.ordersFinished = ordersFinished;
	}

	public List<OrderMenu> getOrders() {
		return orders;
	}

	public void setOrders(List<OrderMenu> orders) {
		this.orders = orders;
	}

}
