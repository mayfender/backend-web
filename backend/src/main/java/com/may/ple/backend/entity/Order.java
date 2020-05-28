package com.may.ple.backend.entity;

import java.util.Date;

import org.bson.types.ObjectId;

public class Order {
	private String id;
	private String name;
	private String orderNumber;
	private Integer type;
	private Integer probNum;
	private Double price;
	private Double todPrice;
	private String symbol;
	private Boolean isParent;
	private ObjectId userId;
	private ObjectId periodId;
	private ObjectId parentId;
	private Date createdDateTime;
	
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
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public ObjectId getUserId() {
		return userId;
	}
	public void setUserId(ObjectId userId) {
		this.userId = userId;
	}
	public Boolean getIsParent() {
		return isParent;
	}
	public void setIsParent(Boolean isParent) {
		this.isParent = isParent;
	}
	public ObjectId getPeriodId() {
		return periodId;
	}
	public void setPeriodId(ObjectId periodId) {
		this.periodId = periodId;
	}
	public ObjectId getParentId() {
		return parentId;
	}
	public void setParentId(ObjectId parentId) {
		this.parentId = parentId;
	}
	public Integer getProbNum() {
		return probNum;
	}
	public void setProbNum(Integer probNum) {
		this.probNum = probNum;
	}
	public Double getTodPrice() {
		return todPrice;
	}
	public void setTodPrice(Double todPrice) {
		this.todPrice = todPrice;
	}
	public Date getCreatedDateTime() {
		return createdDateTime;
	}
	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	
}
