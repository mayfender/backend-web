package com.may.ple.backend.entity;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

public class OrderNew {
	private String id;
	private String name;
	private Integer type;
	private String symbol;
	private ObjectId userId;
	private ObjectId periodId;
	private Date createdDateTime;
	private Boolean isHalfPrice;
	private Integer deviceId;
	private List<OrderFamily> families;

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
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
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
	public ObjectId getPeriodId() {
		return periodId;
	}
	public void setPeriodId(ObjectId periodId) {
		this.periodId = periodId;
	}
	public Date getCreatedDateTime() {
		return createdDateTime;
	}
	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	public Boolean getIsHalfPrice() {
		return isHalfPrice;
	}
	public void setIsHalfPrice(Boolean isHalfPrice) {
		this.isHalfPrice = isHalfPrice;
	}
	public Integer getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}
	public List<OrderFamily> getFamilies() {
		return families;
	}
	public void setFamilies(List<OrderFamily> families) {
		this.families = families;
	}

}
