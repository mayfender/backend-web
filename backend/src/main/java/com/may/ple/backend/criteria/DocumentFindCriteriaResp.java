package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Document;

public class DocumentFindCriteriaResp extends CommonCriteriaResp {
	private List<Document> documents;
	private Long totalItems;
	private List<Map> seizures;
	
	public DocumentFindCriteriaResp(){}
	
	public DocumentFindCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public Long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

	public List<Map> getSeizures() {
		return seizures;
	}

	public void setSeizures(List<Map> seizures) {
		this.seizures = seizures;
	}

}
