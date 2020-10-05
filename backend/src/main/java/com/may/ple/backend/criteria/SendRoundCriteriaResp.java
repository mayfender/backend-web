package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.SendRound;

public class SendRoundCriteriaResp extends CommonCriteriaResp {
	private List<SendRound> dataList;

	public SendRoundCriteriaResp() {}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public SendRoundCriteriaResp(int statusCode) {
		super(statusCode);
	}

	public List<SendRound> getDataList() {
		return dataList;
	}

	public void setDataList(List<SendRound> dataList) {
		this.dataList = dataList;
	}

}
