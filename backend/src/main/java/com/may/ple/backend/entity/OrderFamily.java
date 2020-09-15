package com.may.ple.backend.entity;

import org.bson.types.ObjectId;

public class OrderFamily {
	private ObjectId id;
	private String orderNumber;
	private Integer type;
	private Double price;

	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}

}
