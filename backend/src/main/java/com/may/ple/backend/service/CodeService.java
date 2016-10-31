package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.ActionCodeFindCriteriaReq;
import com.may.ple.backend.criteria.CodeSaveCriteriaReq;
import com.may.ple.backend.criteria.ResultCodeFindCriteriaReq;
import com.may.ple.backend.entity.ActionCode;
import com.may.ple.backend.entity.ResultCode;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;

@Service
public class CodeService {
	private static final Logger LOG = Logger.getLogger(CodeService.class.getName());
	private MongoTemplate template;
	private DbFactory dbFactory;
	
	@Autowired	
	public CodeService(MongoTemplate template, DbFactory dbFactory) {
		this.template = template;
		this.dbFactory = dbFactory;
	}
	
	public List<ActionCode> findActionCode(ActionCodeFindCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Query query = Query.query(Criteria.where("enabled").in(req.getStatuses()));
			query.fields().include("actCode").include("actDesc").include("actMeaning")
						  .include("enabled").include("isPrintNotice");
			
			List<ActionCode> actionCodes = template.find(query, ActionCode.class);			
			
			return actionCodes;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<ResultCode> findResultCode(ResultCodeFindCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Query query = Query.query(Criteria.where("enabled").in(req.getStatuses()));
			query.fields().include("rstCode").include("rstDesc").include("rstMeaning").include("enabled").include("resultGroupId");
			query.with(new Sort("resultGroupId"));

			List<ResultCode> actionCodes = template.find(query, ResultCode.class);			
			
			return actionCodes;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String saveActionCode(CodeSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(this.template);
			ActionCode actionCode;
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			if(StringUtils.isBlank(req.getId())) {
				actionCode = new ActionCode(req.getCode(), req.getDesc(), req.getMeaning(), req.getEnabled());
				actionCode.setCreatedDateTime(date);
				actionCode.setUpdatedDateTime(date);
				actionCode.setCreatedBy(user.getId());	
				actionCode.setIsPrintNotice(req.getIsPrintNotice());
			} else {
				actionCode = template.findOne(Query.query(Criteria.where("id").is(req.getId())), ActionCode.class);
				actionCode.setActCode(req.getCode());
				actionCode.setActDesc(req.getDesc());
				actionCode.setActMeaning(req.getMeaning());
				actionCode.setEnabled(req.getEnabled());
				actionCode.setUpdatedDateTime(date);
				actionCode.setUpdatedBy(user.getId());
				actionCode.setIsPrintNotice(req.getIsPrintNotice());
			}
			
			LOG.debug("Save action code");
			template.save(actionCode);
			
			return actionCode.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String saveResultCode(CodeSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(this.template);
			ResultCode resultCode;
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			if(StringUtils.isBlank(req.getId())) {
				resultCode = new ResultCode(req.getCode(), req.getDesc(), req.getMeaning(), req.getEnabled());
				resultCode.setCreatedDateTime(date);
				resultCode.setUpdatedDateTime(date);
				resultCode.setCreatedBy(user.getId());				
				resultCode.setResultGroupId(req.getResultGroupId());
			} else {
				resultCode = template.findOne(Query.query(Criteria.where("id").is(req.getId())), ResultCode.class);
				resultCode.setRstCode(req.getCode());
				resultCode.setRstDesc(req.getDesc());
				resultCode.setRstMeaning(req.getMeaning());
				resultCode.setEnabled(req.getEnabled());
				resultCode.setUpdatedDateTime(date);
				resultCode.setUpdatedBy(user.getId());
				resultCode.setResultGroupId(req.getResultGroupId());
			}
			
			LOG.debug("Save action code");
			template.save(resultCode);
			
			return resultCode.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteActionCode(String id, String productId) throws Exception {
		try {
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(this.template);
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			ActionCode actionCode = template.findOne(Query.query(Criteria.where("id").is(id)), ActionCode.class);
			actionCode.setEnabled(-1); //--: Define -1 as delete
			actionCode.setUpdatedDateTime(new Date());
			actionCode.setUpdatedBy(user.getId());
			
			template.save(actionCode);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteResultCode(String id, String productId) throws Exception {
		try {
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(this.template);
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			ResultCode resultCode = template.findOne(Query.query(Criteria.where("id").is(id)), ResultCode.class);
			resultCode.setEnabled(-1); //--: Define -1 as delete
			resultCode.setUpdatedDateTime(new Date());
			resultCode.setUpdatedBy(user.getId());
			
			template.save(resultCode);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
