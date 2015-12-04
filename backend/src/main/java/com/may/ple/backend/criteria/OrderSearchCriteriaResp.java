package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.OrderMenu;

public class OrderSearchCriteriaResp extends CommonCriteriaResp {
	private List<OrderMenu> orders;
	
	public OrderSearchCriteriaResp() {}
	
	public OrderSearchCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<OrderMenu> getOrders() {
		return orders;
	}
	public void setOrders(List<OrderMenu> orders) {
		this.orders = orders;
	}

}
