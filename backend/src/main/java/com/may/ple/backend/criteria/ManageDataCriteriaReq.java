package com.may.ple.backend.criteria;


public class ManageDataCriteriaReq {
	private Integer operationId;
	private String productId;
	private String moduleName;
	private String fieldName;
	private Integer conditionName;
	private Integer conditionValue;

	public Integer getOperationId() {
		return operationId;
	}
	public void setOperationId(Integer operationId) {
		this.operationId = operationId;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public Integer getConditionName() {
		return conditionName;
	}
	public void setConditionName(Integer conditionName) {
		this.conditionName = conditionName;
	}
	public Integer getConditionValue() {
		return conditionValue;
	}
	public void setConditionValue(Integer conditionValue) {
		this.conditionValue = conditionValue;
	}

}
