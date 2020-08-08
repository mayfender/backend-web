package com.may.ple.backend.entity;

import java.util.Date;

import org.bson.types.ObjectId;

public class Receiver {
	private String id;
	private String receiverName;
	private String senderName;
	private Date createdDateTime;
	private boolean enabled;
	private int order;
	private ObjectId priceListId;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getReceiverName() {
		return receiverName;
	}
	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
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
	public ObjectId getPriceListId() {
		return priceListId;
	}
	public void setPriceListId(ObjectId priceListId) {
		this.priceListId = priceListId;
	}

}
