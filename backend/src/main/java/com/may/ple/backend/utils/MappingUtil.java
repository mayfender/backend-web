package com.may.ple.backend.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.may.ple.backend.entity.Users;

public class MappingUtil {
	private static final Logger LOG = Logger.getLogger(MappingUtil.class.getName());

	public static List<Map<String, String>> matchUserId(List<Users> users, String userId) {

		if (userId == null)
			return null;

		if (users == null)
			return null;

		List<Map<String, String>> userList;
		Map<String, String> userMap;

		for (Users u : users) {
			if (userId.equals(u.getId())) {
				userList = new ArrayList<>();
				userMap = new HashMap<>();

				userMap.put("id", u.getId());
				userMap.put("username", u.getUsername());
				userMap.put("showname", u.getShowname());
				userMap.put("firstName", u.getFirstName());
				userMap.put("lastName", u.getLastName());
				userMap.put("phone", u.getPhoneNumber());
				userMap.put("phoneExt", u.getPhoneExt());

				userList.add(userMap);

				return userList;
			}
		}

		LOG.debug("Not found user from id " + userId);
		return null;
	}

}
