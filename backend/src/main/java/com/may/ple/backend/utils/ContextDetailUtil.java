package com.may.ple.backend.utils;

import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.may.ple.backend.entity.Users;

public class ContextDetailUtil {
	private static final Logger LOG = Logger.getLogger(ContextDetailUtil.class.getName());
	
	public static Users getCurrentUser(MongoTemplate templateCenter) {
		try {
			LOG.debug("Get user from context");
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String username = authentication.getName();
			LOG.debug("User from context: " + username);
			
			LOG.debug("Get user id from database");
			Users user = templateCenter.findOne(Query.query(Criteria.where("username").is(username)), Users.class);
			LOG.debug("User id from database: " + user.getId());
			return user;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
