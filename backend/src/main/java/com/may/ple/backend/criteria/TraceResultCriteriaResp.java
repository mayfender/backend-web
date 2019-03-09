package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ActionCode;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.DymSearch;
import com.may.ple.backend.entity.ResultCode;
import com.may.ple.backend.entity.ResultCodeGroup;
import com.may.ple.backend.entity.TraceResultReportFile;
import com.may.ple.backend.entity.Users;

public class TraceResultCriteriaResp extends CommonCriteriaResp {
	private List<Map> traceDatas;
	private List<ColumnFormat> headers;
	private Long totalItems;
	private Double appointAmountTotal;
	private List<Users> users;
	private List<ActionCode> actionCodes;
	private List<ResultCode> resultCodes;
	private List<ResultCodeGroup> resultCodeGroups;
	private Boolean isDisableNoticePrint;
	private Boolean isTraceExportExcel;
	private Boolean isTraceExportTxt;
	private List<Map> dymList;
	private List<TraceResultReportFile> uploadTemplates;
	private Integer createdByLog;
	private List<DymSearch> dymSearch;
	
	public TraceResultCriteriaResp(){}
	
	public TraceResultCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getTotalItems() {
		return totalItems;
	}
	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

	public List<ColumnFormat> getHeaders() {
		return headers;
	}

	public void setHeaders(List<ColumnFormat> headers) {
		this.headers = headers;
	}

	public List<Users> getUsers() {
		return users;
	}

	public void setUsers(List<Users> users) {
		this.users = users;
	}

	public List<Map> getTraceDatas() {
		return traceDatas;
	}

	public void setTraceDatas(List<Map> traceDatas) {
		this.traceDatas = traceDatas;
	}

	public Double getAppointAmountTotal() {
		return appointAmountTotal;
	}

	public void setAppointAmountTotal(Double appointAmountTotal) {
		this.appointAmountTotal = appointAmountTotal;
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

	public Boolean getIsDisableNoticePrint() {
		return isDisableNoticePrint;
	}

	public void setIsDisableNoticePrint(Boolean isDisableNoticePrint) {
		this.isDisableNoticePrint = isDisableNoticePrint;
	}

	public Boolean getIsTraceExportExcel() {
		return isTraceExportExcel;
	}

	public void setIsTraceExportExcel(Boolean isTraceExportExcel) {
		this.isTraceExportExcel = isTraceExportExcel;
	}

	public Boolean getIsTraceExportTxt() {
		return isTraceExportTxt;
	}

	public void setIsTraceExportTxt(Boolean isTraceExportTxt) {
		this.isTraceExportTxt = isTraceExportTxt;
	}

	public List<Map> getDymList() {
		return dymList;
	}

	public void setDymList(List<Map> dymList) {
		this.dymList = dymList;
	}

	public List<TraceResultReportFile> getUploadTemplates() {
		return uploadTemplates;
	}

	public void setUploadTemplates(List<TraceResultReportFile> uploadTemplates) {
		this.uploadTemplates = uploadTemplates;
	}

	public Integer getCreatedByLog() {
		return createdByLog;
	}

	public void setCreatedByLog(Integer createdByLog) {
		this.createdByLog = createdByLog;
	}

	public List<DymSearch> getDymSearch() {
		return dymSearch;
	}

	public void setDymSearch(List<DymSearch> dymSearch) {
		this.dymSearch = dymSearch;
	}

}
