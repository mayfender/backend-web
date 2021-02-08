package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DMSCriteriaResp extends CommonCriteriaResp {
	private List<Map> customers;
	private Map customer;
	private long totalItems;

	public DMSCriteriaResp() {}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Map> getCustomers() {
		return customers;
	}

	public void setCustomers(List<Map> customers) {
		this.customers = customers;
	}

	public long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(long totalItems) {
		this.totalItems = totalItems;
	}

	public Map getCustomer() {
		return customer;
	}

	public void setCustomer(Map customer) {
		this.customer = customer;
	}

}
