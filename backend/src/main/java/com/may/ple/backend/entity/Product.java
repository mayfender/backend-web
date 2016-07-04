package com.may.ple.backend.entity;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Product {
	private String id;
	private String productName;
	private Integer enabled;
	private Date createdDateTime;
	private Date updatedDateTime;
	private Database database;
	private List<ColumnFormat> columnFormats;
	private ProductSetting productSetting;
	private List<GroupData> groupDatas;
	
	public Product(){}
	
	public Product(String productName, Integer enabled, Date createdDateTime, Date updatedDateTime, Database database) {
		this.productName = productName;
		this.enabled = enabled;
		this.createdDateTime = createdDateTime;
		this.updatedDateTime = updatedDateTime;
		this.database = database;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public Date getCreatedDateTime() {
		return createdDateTime;
	}
	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	public Date getUpdatedDateTime() {
		return updatedDateTime;
	}
	public void setUpdatedDateTime(Date updatedDateTime) {
		this.updatedDateTime = updatedDateTime;
	}
	public Database getDatabase() {
		return database;
	}
	public void setDatabase(Database database) {
		this.database = database;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getEnabled() {
		return enabled;
	}
	public void setEnabled(Integer enabled) {
		this.enabled = enabled;
	}

	public List<ColumnFormat> getColumnFormats() {
		return columnFormats;
	}

	public void setColumnFormats(List<ColumnFormat> columnFormats) {
		this.columnFormats = columnFormats;
	}

	public ProductSetting getProductSetting() {
		return productSetting;
	}

	public void setProductSetting(ProductSetting productSetting) {
		this.productSetting = productSetting;
	}

	public List<GroupData> getGroupDatas() {
		return groupDatas;
	}

	public void setGroupDatas(List<GroupData> groupDatas) {
		this.groupDatas = groupDatas;
	}
	
}
