package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class MenuSaveCriteriaReq {	
	private Long id;
	private String name;
	private Integer price;
	private Integer status;
	private Boolean isRecommented;
	private String imgContent;
	private String imgName;
	private Boolean isChangedImg;
	private Long menuTypeId;
	
	@Override
	public String toString() {
		
		ReflectionToStringBuilder stringBuilder = new ReflectionToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
		stringBuilder.setAppendStatics(true);
		stringBuilder.setAppendTransients(true);
		stringBuilder.setExcludeFieldNames("imgContent");
		
		return stringBuilder.toString();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Boolean getIsRecommented() {
		return isRecommented;
	}
	public void setIsRecommented(Boolean isRecommented) {
		this.isRecommented = isRecommented;
	}
	public String getImgContent() {
		return imgContent;
	}
	public void setImgContent(String imgContent) {
		this.imgContent = imgContent;
	}
	public String getImgName() {
		return imgName;
	}
	public void setImgName(String imgName) {
		this.imgName = imgName;
	}
	public Boolean getIsChangedImg() {
		return isChangedImg;
	}
	public void setIsChangedImg(Boolean isChangedImg) {
		this.isChangedImg = isChangedImg;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getMenuTypeId() {
		return menuTypeId;
	}
	public void setMenuTypeId(Long menuTypeId) {
		this.menuTypeId = menuTypeId;
	}

}
