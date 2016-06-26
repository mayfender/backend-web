package com.may.ple.backend.entity;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ImgData {
	private byte[] imgContent;
	private String imgName;
	
	public ImgData() {}
	
	public ImgData(String imgName, byte[] imgContent) {
		this.imgName = imgName;
		this.imgContent = imgContent;
	}
	
	@Override
	public String toString() {
		
		ReflectionToStringBuilder stringBuilder = new ReflectionToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
		stringBuilder.setAppendStatics(true);
		stringBuilder.setAppendTransients(true);
		stringBuilder.setExcludeFieldNames("imgContent");
		
		return stringBuilder.toString();
	}
	
	public String getImgName() {
		return imgName;
	}
	public void setImgName(String imgName) {
		this.imgName = imgName;
	}

	public void setImgContent(byte[] imgContent) {
		this.imgContent = imgContent;
	}

	public byte[] getImgContent() {
		return imgContent;
	}

}
