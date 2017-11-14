package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;

public class FileCommonCriteriaResp extends CommonCriteriaResp {
	private List<Map> files;
	private Map<String, List<Map>> checkMapList;
	private List<Map> checkList;
	private Long totalItems;
	private List<ColumnFormat> colDateTypes;
	private List<String> colNotFounds;
	private List<ColumnFormat> headers;
	private String idCardNoColumnName;
	private String birthDateColumnName;
	
	public FileCommonCriteriaResp(){}
	
	public FileCommonCriteriaResp(int statusCode) {
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

	public List<Map> getFiles() {
		return files;
	}

	public void setFiles(List<Map> files) {
		this.files = files;
	}

	public List<ColumnFormat> getColDateTypes() {
		return colDateTypes;
	}

	public void setColDateTypes(List<ColumnFormat> colDateTypes) {
		this.colDateTypes = colDateTypes;
	}

	public List<String> getColNotFounds() {
		return colNotFounds;
	}

	public void setColNotFounds(List<String> colNotFounds) {
		this.colNotFounds = colNotFounds;
	}

	public List<ColumnFormat> getHeaders() {
		return headers;
	}

	public void setHeaders(List<ColumnFormat> headers) {
		this.headers = headers;
	}

	public String getIdCardNoColumnName() {
		return idCardNoColumnName;
	}

	public void setIdCardNoColumnName(String idCardNoColumnName) {
		this.idCardNoColumnName = idCardNoColumnName;
	}

	public String getBirthDateColumnName() {
		return birthDateColumnName;
	}

	public void setBirthDateColumnName(String birthDateColumnName) {
		this.birthDateColumnName = birthDateColumnName;
	}

	public Map<String, List<Map>> getCheckMapList() {
		return checkMapList;
	}

	public void setCheckMapList(Map<String, List<Map>> checkMapList) {
		this.checkMapList = checkMapList;
	}

	public List<Map> getCheckList() {
		return checkList;
	}

	public void setCheckList(List<Map> checkList) {
		this.checkList = checkList;
	}

}
