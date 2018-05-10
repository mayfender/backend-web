package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ProductSetting;

public class ProductSettingCriteriaResp extends CommonCriteriaResp {
	private ProductSetting productSetting;
	
	public ProductSettingCriteriaResp() {}
	
	public ProductSettingCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public ProductSetting getProductSetting() {
		return productSetting;
	}

	public void setProductSetting(ProductSetting productSetting) {
		this.productSetting = productSetting;
	}
	
}
