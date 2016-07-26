package com.may.ple.backend.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.may.ple.backend.model.DbFactory;

@Service
public class TraceWorkService {
	private static final Logger LOG = Logger.getLogger(TraceWorkService.class.getName());
	private MongoTemplate template;
	private DbFactory dbFactory;
	
	@Autowired	
	public TraceWorkService(MongoTemplate template, DbFactory dbFactory) {
		this.template = template;
		this.dbFactory = dbFactory;
	}
	
	/*public List<ActionCode> findActionCode(ActionCodeFindCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Query query = Query.query(Criteria.where("enabled").ne(-1));
			query.fields().include("code").include("desc").include("meaning").include("enabled");
			
			List<ActionCode> actionCodes = template.find(query, ActionCode.class);			
			
			return actionCodes;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}*/
	
	/*public List<ResultCode> findResultCode(ResultCodeFindCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Query query = Query.query(Criteria.where("enabled").ne(-1));
			query.fields().include("code").include("desc").include("meaning").include("enabled").include("resultGroupId");
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
			} else {
				actionCode = template.findOne(Query.query(Criteria.where("id").is(req.getId())), ActionCode.class);
				actionCode.setCode(req.getCode());
				actionCode.setDesc(req.getDesc());
				actionCode.setMeaning(req.getMeaning());
				actionCode.setEnabled(req.getEnabled());
				actionCode.setUpdatedDateTime(date);
				actionCode.setUpdatedBy(user.getId());
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
				resultCode.setCode(req.getCode());
				resultCode.setDesc(req.getDesc());
				resultCode.setMeaning(req.getMeaning());
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
	}*/
		
}
