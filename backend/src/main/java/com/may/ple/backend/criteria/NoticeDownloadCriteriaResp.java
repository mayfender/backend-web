package com.may.ple.backend.criteria;

import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class NoticeDownloadCriteriaResp extends CommonCriteriaResp {
	private StreamingOutput fileData;
	private String fileName;
	private String fileType;
	
	public NoticeDownloadCriteriaResp(){}
	
	public NoticeDownloadCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public StreamingOutput getFileData() {
		return fileData;
	}

	public void setFileData(StreamingOutput fileData) {
		this.fileData = fileData;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

}
