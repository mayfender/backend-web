package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.DymSearch;
import com.may.ple.backend.entity.ForecastResultReportFile;
import com.may.ple.backend.entity.Users;

public class ForecastResultCriteriaResp extends CommonCriteriaResp {
	private List<Map> forecastDatas;
	private List<ColumnFormat> headers;
	private Long totalItems;
	private List<Users> users;
	private List<ForecastResultReportFile> uploadTemplates;
	private Integer createdByLog;
	private List<Map> dymList;
	private List<DymSearch> dymSearch;
	
	public ForecastResultCriteriaResp(){}
	
	public ForecastResultCriteriaResp(int statusCode) {
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

	public List<ForecastResultReportFile> getUploadTemplates() {
		return uploadTemplates;
	}

	public void setUploadTemplates(List<ForecastResultReportFile> uploadTemplates) {
		this.uploadTemplates = uploadTemplates;
	}

	public List<Map> getForecastDatas() {
		return forecastDatas;
	}

	public void setForecastDatas(List<Map> forecastDatas) {
		this.forecastDatas = forecastDatas;
	}

	public Integer getCreatedByLog() {
		return createdByLog;
	}

	public void setCreatedByLog(Integer createdByLog) {
		this.createdByLog = createdByLog;
	}

	public List<Map> getDymList() {
		return dymList;
	}

	public void setDymList(List<Map> dymList) {
		this.dymList = dymList;
	}

	public List<DymSearch> getDymSearch() {
		return dymSearch;
	}

	public void setDymSearch(List<DymSearch> dymSearch) {
		this.dymSearch = dymSearch;
	}

}
