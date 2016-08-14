package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TraceResultCriteriaReq {
	private String id;
	private String taskFileId;
	private String productId;
	private Integer currentPage;
	private Integer itemsPerPage;
	private String columnName;
	private String order;
	private Integer methodId;
	private String calColumn;
	private String keyword;
	private String owner;
	private List<Map<String, String>> usernames;
	private List<String> transferUsernames;
	private List<String> taskIds;
	private Integer taskType;
	private String idCardNo;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Integer getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public Integer getItemsPerPage() {
		return itemsPerPage;
	}

	public void setItemsPerPage(Integer itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTaskFileId() {
		return taskFileId;
	}

	public void setTaskFileId(String taskFileId) {
		this.taskFileId = taskFileId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public Integer getMethodId() {
		return methodId;
	}

	public void setMethodId(Integer methodId) {
		this.methodId = methodId;
	}

	public String getCalColumn() {
		return calColumn;
	}

	public void setCalColumn(String calColumn) {
		this.calColumn = calColumn;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<String> getTaskIds() {
		return taskIds;
	}

	public void setTaskIds(List<String> taskIds) {
		this.taskIds = taskIds;
	}

	public List<Map<String, String>> getUsernames() {
		return usernames;
	}

	public void setUsernames(List<Map<String, String>> usernames) {
		this.usernames = usernames;
	}

	public Integer getTaskType() {
		return taskType;
	}

	public void setTaskType(Integer taskType) {
		this.taskType = taskType;
	}

	public List<String> getTransferUsernames() {
		return transferUsernames;
	}

	public void setTransferUsernames(List<String> transferUsernames) {
		this.transferUsernames = transferUsernames;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getIdCardNo() {
		return idCardNo;
	}

	public void setIdCardNo(String idCardNo) {
		this.idCardNo = idCardNo;
	}

}
