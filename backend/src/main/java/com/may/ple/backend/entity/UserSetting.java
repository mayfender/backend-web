package com.may.ple.backend.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class UserSetting {
	private String currentProduct;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getCurrentProduct() {
		return currentProduct;
	}

	public void setCurrentProduct(String currentProduct) {
		this.currentProduct = currentProduct;
	}
	
}
