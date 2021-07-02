package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ListSaveCriteriaReq {
	private String id;
	private String name;
	private String columnName;
	private String fieldName;
	private String relatedFieldName;
	private Integer order;
	private Integer enabled;
	private String productId;
	private Boolean isGroup;
	private Integer type;

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

	public Integer getEnabled() {
		return enabled;
	}

	public void setEnabled(Integer enabled) {
		this.enabled = enabled;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Boolean getIsGroup() {
		return isGroup;
	}

	public void setIsGroup(Boolean isGroup) {
		this.isGroup = isGroup;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public String getRelatedFieldName() {
		return relatedFieldName;
	}

	public void setRelatedFieldName(String relatedFieldName) {
		this.relatedFieldName = relatedFieldName;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
