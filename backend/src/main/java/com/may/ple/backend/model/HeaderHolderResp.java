package com.may.ple.backend.model;

import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFRow;

import com.mongodb.BasicDBObject;

public class HeaderHolderResp {
	public Map<String, HeaderHolder> header;
	public BasicDBObject fields;
	public XSSFRow rowCopy;
	public String delimiter;
	public String yearType;
	
	public HeaderHolderResp(Map<String, HeaderHolder> header, BasicDBObject fields, XSSFRow rowCopy, String delimiter, String yearType) {
		this.header = header;
		this.fields = fields;
		this.rowCopy = rowCopy;
		this.delimiter = delimiter;
		this.yearType = yearType;
	}
}