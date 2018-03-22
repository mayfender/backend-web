package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ActionCode;
import com.may.ple.backend.entity.Address;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.GroupData;
import com.may.ple.backend.entity.NoticeFile;
import com.may.ple.backend.entity.ResultCode;
import com.may.ple.backend.entity.ResultCodeGroup;
import com.may.ple.backend.model.RelatedData;
import com.may.ple.backend.model.SumPayment;

public class TaskDetailViewCriteriaResp extends CommonCriteriaResp {
	private Map taskDetail;
	private List<Map> taskDetails;
	private Map<Integer, List<ColumnFormat>> ColFormMap;
	private List<GroupData> groupDatas;
	private Map<String, RelatedData> relatedData;
	private List<ActionCode> actionCodes;
	private List<ResultCode> resultCodes;
	private List<ResultCodeGroup> resultCodeGroups;
	private TraceFindCriteriaResp traceResp;
	private List<Address> addresses;
	private List<Map> paymentDetails;
	private Long paymentTotalItems;
	private String comment;
	private Boolean isDisableNoticePrint;
	private Boolean isHideComment;
	private List<NoticeFile> noticeFiles;
	private List<SumPayment> paymentSums;
	private List<Map> dymList;
	private Integer createdByLog;
	private Map<String, String> calParams;
	
	public TaskDetailViewCriteriaResp(){}
	
	public TaskDetailViewCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Map getTaskDetail() {
		return taskDetail;
	}

	public void setTaskDetail(Map taskDetail) {
		this.taskDetail = taskDetail;
	}

	public List<GroupData> getGroupDatas() {
		return groupDatas;
	}

	public void setGroupDatas(List<GroupData> groupDatas) {
		this.groupDatas = groupDatas;
	}

	public Map<Integer, List<ColumnFormat>> getColFormMap() {
		return ColFormMap;
	}

	public void setColFormMap(Map<Integer, List<ColumnFormat>> colFormMap) {
		ColFormMap = colFormMap;
	}

	public Map<String, RelatedData> getRelatedData() {
		return relatedData;
	}

	public void setRelatedData(Map<String, RelatedData> relatedData) {
		this.relatedData = relatedData;
	}

	public List<ActionCode> getActionCodes() {
		return actionCodes;
	}

	public void setActionCodes(List<ActionCode> actionCodes) {
		this.actionCodes = actionCodes;
	}

	public List<ResultCode> getResultCodes() {
		return resultCodes;
	}

	public void setResultCodes(List<ResultCode> resultCodes) {
		this.resultCodes = resultCodes;
	}

	public List<ResultCodeGroup> getResultCodeGroups() {
		return resultCodeGroups;
	}

	public void setResultCodeGroups(List<ResultCodeGroup> resultCodeGroups) {
		this.resultCodeGroups = resultCodeGroups;
	}

	public TraceFindCriteriaResp getTraceResp() {
		return traceResp;
	}

	public void setTraceResp(TraceFindCriteriaResp traceResp) {
		this.traceResp = traceResp;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	public List<Map> getPaymentDetails() {
		return paymentDetails;
	}

	public void setPaymentDetails(List<Map> paymentDetails) {
		this.paymentDetails = paymentDetails;
	}

	public Long getPaymentTotalItems() {
		return paymentTotalItems;
	}

	public void setPaymentTotalItems(Long paymentTotalItems) {
		this.paymentTotalItems = paymentTotalItems;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Boolean getIsDisableNoticePrint() {
		return isDisableNoticePrint;
	}

	public void setIsDisableNoticePrint(Boolean isDisableNoticePrint) {
		this.isDisableNoticePrint = isDisableNoticePrint;
	}

	public List<NoticeFile> getNoticeFiles() {
		return noticeFiles;
	}

	public void setNoticeFiles(List<NoticeFile> noticeFiles) {
		this.noticeFiles = noticeFiles;
	}

	public List<Map> getTaskDetails() {
		return taskDetails;
	}

	public void setTaskDetails(List<Map> taskDetails) {
		this.taskDetails = taskDetails;
	}

	public List<SumPayment> getPaymentSums() {
		return paymentSums;
	}

	public void setPaymentSums(List<SumPayment> paymentSums) {
		this.paymentSums = paymentSums;
	}

	public List<Map> getDymList() {
		return dymList;
	}

	public void setDymList(List<Map> dymList) {
		this.dymList = dymList;
	}

	public Boolean getIsHideComment() {
		return isHideComment;
	}

	public void setIsHideComment(Boolean isHideComment) {
		this.isHideComment = isHideComment;
	}

	public Integer getCreatedByLog() {
		return createdByLog;
	}

	public void setCreatedByLog(Integer createdByLog) {
		this.createdByLog = createdByLog;
	}

	public Map<String, String> getCalParams() {
		return calParams;
	}

	public void setCalParams(Map<String, String> calParams) {
		this.calParams = calParams;
	}

}
