package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Users;

public class ChattingCriteriaResp extends CommonCriteriaResp {
	private List<Users> friends;
	
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

}
