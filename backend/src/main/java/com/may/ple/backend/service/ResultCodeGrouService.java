package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.ResultCodeGroupFindCriteriaReq;
import com.may.ple.backend.criteria.ResultCodeGroupSaveCriteriaReq;
import com.may.ple.backend.entity.ResultCodeGroup;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;

@Service
public class ResultCodeGrouService {
	private static final Logger LOG = Logger.getLogger(ResultCodeGrouService.class.getName());
	private MongoTemplate template;
	private DbFactory dbFactory;
	
	@Autowired	
	public ResultCodeGrouService(MongoTemplate template, DbFactory dbFactory) {
		this.template = template;
		this.dbFactory = dbFactory;
	}
	
	public List<ResultCodeGroup> find(ResultCodeGroupFindCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Query query = new Query();
			query.fields().include("name");

			List<ResultCodeGroup> resultCodeGroups = template.find(query, ResultCodeGroup.class);			
			
			return resultCodeGroups;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String save(ResultCodeGroupSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(this.template);
			ResultCodeGroup resultCodeGroup;
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			if(StringUtils.isBlank(req.getId())) {
				resultCodeGroup = new ResultCodeGroup(req.getName());
				resultCodeGroup.setCreatedDateTime(date);
				resultCodeGroup.setUpdatedDateTime(date);
				resultCodeGroup.setCreatedBy(user.getId());				
			} else {
				resultCodeGroup = template.findOne(Query.query(Criteria.where("id").is(req.getId())), ResultCodeGroup.class);
				resultCodeGroup.setName(req.getName());
				resultCodeGroup.setUpdatedDateTime(date);
				resultCodeGroup.setUpdatedBy(user.getId());
			}
			
			LOG.debug("Save action code");
			template.save(resultCodeGroup);
			
			return resultCodeGroup.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void delete(String id, String productId) throws Exception {
		try {
			
			LOG.debug("Get user");
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			template.remove(Query.query(Criteria.where("id").is(id)), ResultCodeGroup.class);
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
			
}
