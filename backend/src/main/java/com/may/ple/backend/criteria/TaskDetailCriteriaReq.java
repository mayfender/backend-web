package com.may.ple.backend.criteria;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TaskDetailCriteriaReq {
	private String id;
	private String taskFileId;
	private String productId;
	private Integer currentPage;
	private Integer itemsPerPage;
	private String columnName;
	private String order;
	private Integer methodId;
	private String keyword;
	private String tag;
	private String owner;
	private Boolean isActive;
	private List<Map<String, String>> usernames;
	private List<String> transferUsernames;
	private List<String> taskIds;
	private Integer taskType;
	private String fromPage;
	private String idCardNo;
	private List<String> searchIds;
	private String dateColumnName;
	private Date dateFrom;
	private Date dateTo;
	private String actionType;
	private Boolean isPgs;
	private Boolean isNoTrace;
	private String fileId;
	private String codeName;
	private String codeValue;
	private String kysGroup;
	
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

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
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

	public String getFromPage() {
		return fromPage;
	}

	public void setFromPage(String fromPage) {
		this.fromPage = fromPage;
	}

	public String getIdCardNo() {
		return idCardNo;
	}

	public void setIdCardNo(String idCardNo) {
		this.idCardNo = idCardNo;
	}

	public List<String> getSearchIds() {
		return searchIds;
	}

	public void setSearchIds(List<String> searchIds) {
		this.searchIds = searchIds;
	}

	public String getDateColumnName() {
		return dateColumnName;
	}

	public void setDateColumnName(String dateColumnName) {
		this.dateColumnName = dateColumnName;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Boolean getIsPgs() {
		return isPgs;
	}

	public void setIsPgs(Boolean isPgs) {
		this.isPgs = isPgs;
	}

	public Boolean getIsNoTrace() {
		return isNoTrace;
	}

	public void setIsNoTrace(Boolean isNoTrace) {
		this.isNoTrace = isNoTrace;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getCodeName() {
		return codeName;
	}

	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}

	public String getCodeValue() {
		return codeValue;
	}

	public void setCodeValue(String codeValue) {
		this.codeValue = codeValue;
	}

	public String getKysGroup() {
		return kysGroup;
	}

	public void setKysGroup(String kysGroup) {
		this.kysGroup = kysGroup;
	}

}
