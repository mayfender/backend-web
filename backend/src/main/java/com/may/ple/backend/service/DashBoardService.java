package com.may.ple.backend.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.DashBoardCriteriaReq;
import com.may.ple.backend.entity.ResultCodeGroup;
import com.may.ple.backend.model.DbFactory;

@Service
public class DashBoardService {
	private static final Logger LOG = Logger.getLogger(DashBoardService.class.getName());
	private MongoTemplate template;
	private DbFactory dbFactory;
	
	@Autowired	
	public DashBoardService(MongoTemplate template, DbFactory dbFactory) {
		this.template = template;
		this.dbFactory = dbFactory;
	}
	
	public void traceCount(DashBoardCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Query query = new Query();
			query.fields().include("name");

			List<ResultCodeGroup> resultCodeGroups = template.find(query, ResultCodeGroup.class);			
			
			return ;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
			
}
