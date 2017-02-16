package com.may.ple.backend.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bson.types.ObjectId;

public class TraceWorkUpdatedHistory extends TraceWork{
	//--: Link id to main trace-work
	private ObjectId traceWorkId;
	private String action;
	
	public TraceWorkUpdatedHistory(){}
	
	public TraceWorkUpdatedHistory(String resultText, String tel, ObjectId actionCode, ObjectId resultCode, Date appointDate, Date nextTimeDate) {
		super(resultText, tel, actionCode, resultCode, appointDate, nextTimeDate);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public ObjectId getTraceWorkId() {
		return traceWorkId;
	}

	public void setTraceWorkId(ObjectId traceWorkId) {
		this.traceWorkId = traceWorkId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
}
