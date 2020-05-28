package com.may.ple.backend.entity;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

public class OrderName {
	private String id;
	private List<Map> names;
	private ObjectId userId;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<Map> getNames() {
		return names;
	}
	public void setNames(List<Map> names) {
		this.names = names;
	}
	public ObjectId getUserId() {
		return userId;
	}
	public void setUserId(ObjectId userId) {
		this.userId = userId;
	}
	
}
