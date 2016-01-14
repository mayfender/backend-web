package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.MenuType;


public class EditMenuDataCriteriaResp extends CommonCriteriaResp {
	private String imgBase64;
	private String imgType;
	private String imgName;
	private List<MenuType> menuTypeChilds;
	
	public EditMenuDataCriteriaResp() {}
	
	public EditMenuDataCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		
		ReflectionToStringBuilder stringBuilder = new ReflectionToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
		stringBuilder.setAppendStatics(true);
		stringBuilder.setAppendTransients(true);
		stringBuilder.setExcludeFieldNames("imgBase64");
		
		return stringBuilder.toString();
	}

	public String getImgBase64() {
		return imgBase64;
	}
	public void setImgBase64(String imgBase64) {
		this.imgBase64 = imgBase64;
	}
	public String getImgType() {
		return imgType;
	}
	public void setImgType(String imgType) {
		this.imgType = imgType;
	}
	public String getImgName() {
		return imgName;
	}
	public void setImgName(String imgName) {
		this.imgName = imgName;
	}
	public List<MenuType> getMenuTypeChilds() {
		return menuTypeChilds;
	}
	public void setMenuTypeChilds(List<MenuType> menuTypeChilds) {
		this.menuTypeChilds = menuTypeChilds;
	}

}
