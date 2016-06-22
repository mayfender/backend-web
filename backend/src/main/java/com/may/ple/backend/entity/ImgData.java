package com.may.ple.backend.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Transient;

public class ImgData {
	private byte[] imgContent;
	private String imgName;
	@Transient
	private String imgContentBase64;
	
	public ImgData() {}
	
	public ImgData(String imgName, byte[] imgContent) {
		this.imgName = imgName;
		this.imgContent = imgContent;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	
	public String getImgName() {
		return imgName;
	}
	public void setImgName(String imgName) {
		this.imgName = imgName;
	}

	public String getImgContentBase64() {
		return imgContentBase64;
	}

	public void setImgContentBase64(String imgContentBase64) {
		this.imgContentBase64 = imgContentBase64;
	}

	public void setImgContent(byte[] imgContent) {
		this.imgContent = imgContent;
	}

	public byte[] getImgContent() {
		return imgContent;
	}

}
