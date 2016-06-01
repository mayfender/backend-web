package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.NewTaskFile;
import com.may.ple.backend.entity.Product;

public class NewTaskCriteriaResp extends CommonCriteriaResp {
	private List<NewTaskFile> files;
	private List<Product> products;
	private Long totalItems;
	
	public NewTaskCriteriaResp(){}
	
	public NewTaskCriteriaResp(int statusCode) {
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
	public List<NewTaskFile> getFiles() {
		return files;
	}
	public void setFiles(List<NewTaskFile> files) {
		this.files = files;
	}
	public List<Product> getProducts() {
		return products;
	}
	public void setProducts(List<Product> products) {
		this.products = products;
	}

}
