package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Users;

public class NotificationCriteriaResp extends CommonCriteriaResp {
	private List<Map> notificationList;
	private List<Map> groupAlertNum;
	private Long totalItems;
	private List<Users> users;
	
	public NotificationCriteriaResp() {}
	
	public NotificationCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

	public List<Map> getNotificationList() {
		return notificationList;
	}

	public void setNotificationList(List<Map> notificationList) {
		this.notificationList = notificationList;
	}

	public List<Map> getGroupAlertNum() {
		return groupAlertNum;
	}

	public void setGroupAlertNum(List<Map> groupAlertNum) {
		this.groupAlertNum = groupAlertNum;
	}

	public List<Users> getUsers() {
		return users;
	}

	public void setUsers(List<Users> users) {
		this.users = users;
	}

}
