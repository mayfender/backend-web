package com.may.ple.backend.criteria;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ImgData;
import com.may.ple.backend.entity.Users;

public class ChattingCriteriaResp extends CommonCriteriaResp {
	private List<Users> friends;
	private List<Map> mapData;
	private Map<String, ImgData> mapImg;
	private String chattingId;
	private Date createdDateTime;
	
	public ChattingCriteriaResp(){}
	
	public ChattingCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Users> getFriends() {
		return friends;
	}

	public void setFriends(List<Users> friends) {
		this.friends = friends;
	}

	public List<Map> getMapData() {
		return mapData;
	}

	public void setMapData(List<Map> mapData) {
		this.mapData = mapData;
	}

	public Map<String, ImgData> getMapImg() {
		return mapImg;
	}

	public void setMapImg(Map<String, ImgData> mapImg) {
		this.mapImg = mapImg;
	}

	public String getChattingId() {
		return chattingId;
	}

	public void setChattingId(String chattingId) {
		this.chattingId = chattingId;
	}

	public Date getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

}
