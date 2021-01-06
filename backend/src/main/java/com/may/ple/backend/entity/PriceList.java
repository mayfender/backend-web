package com.may.ple.backend.entity;

import java.util.Date;
import java.util.Map;

public class PriceList {
	private String id;
	private String priceListName;
	private Date createdDateTime;
	private boolean enabled;
	private int order;
	private Map priceData;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPriceListName() {
		return priceListName;
	}
	public void setPriceListName(String priceListName) {
		this.priceListName = priceListName;
	}
	public Date getCreatedDateTime() {
		return createdDateTime;
	}
	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public Map getPriceData() {
		return priceData;
	}
	public void setPriceData(Map priceData) {
		this.priceData = priceData;
	}

}
