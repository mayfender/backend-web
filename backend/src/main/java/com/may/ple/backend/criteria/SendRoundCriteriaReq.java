package com.may.ple.backend.criteria;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SendRoundCriteriaReq {
	private String id;
	private String name;
	private Date limitedTime;
	private String dealerId;
	private Boolean enabled;
	private List<Map> orderData;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getLimitedTime() {
		return limitedTime;
	}

	public void setLimitedTime(Date limitedTime) {
		this.limitedTime = limitedTime;
	}

	public String getDealerId() {
		return dealerId;
	}

	public void setDealerId(String dealerId) {
		this.dealerId = dealerId;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public List<Map> getOrderData() {
		return orderData;
	}

	public void setOrderData(List<Map> orderData) {
		this.orderData = orderData;
	}


}
