package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Users;

public class UserLogCriteriaResp extends CommonCriteriaResp {
	private List<Users> users;
	private List<Map> logs;
	private List<Map> actionList;
	private Long totalItems;

	public UserLogCriteriaResp() {}

	public UserLogCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Users> getUsers() {
		return users;
	}

	public void setUsers(List<Users> users) {
		this.users = users;
	}

	public List<Map> getLogs() {
		return logs;
	}

	public void setLogs(List<Map> logs) {
		this.logs = logs;
	}

	public List<Map> getActionList() {
		return actionList;
	}

	public void setActionList(List<Map> actionList) {
		this.actionList = actionList;
	}

	public Long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

}
