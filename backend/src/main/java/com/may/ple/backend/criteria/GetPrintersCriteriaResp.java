package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class GetPrintersCriteriaResp extends CommonCriteriaResp {
	private List<String> printers;
	
	public GetPrintersCriteriaResp(){}
	
	public GetPrintersCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<String> getPrinters() {
		return printers;
	}

	public void setPrinters(List<String> printers) {
		this.printers = printers;
	}

}
