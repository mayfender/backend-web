package com.may.ple.backend.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.UserLogCriteriaReq;
import com.may.ple.backend.model.DbFactory;

@Service
public class UserLogService {
	private static final Logger LOG = Logger.getLogger(UserLogService.class.getName());
	private DbFactory dbFactory;

	@Autowired
	public UserLogService(DbFactory dbFactory) {
		this.dbFactory = dbFactory;
	}

	public List<Map> getLog(UserLogCriteriaReq req) {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			return template.find(Query.query(Criteria.where("userId").is(new ObjectId(req.getUserId()))), Map.class, "userLog");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
