package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.DymListDetGroupSaveCriteriaReq;
import com.may.ple.backend.criteria.DymListFindCriteriaReq;
import com.may.ple.backend.criteria.LisDetSaveCriteriaReq;
import com.may.ple.backend.criteria.ListSaveCriteriaReq;
import com.may.ple.backend.entity.DymList;
import com.may.ple.backend.entity.DymListDet;
import com.may.ple.backend.entity.DymListDetGroup;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;

@Service
public class DymListService {
	private static final Logger LOG = Logger.getLogger(DymListService.class.getName());
	private MongoTemplate template;
	private DbFactory dbFactory;
	
	@Autowired	
	public DymListService(MongoTemplate template, DbFactory dbFactory) {
		this.template = template;
		this.dbFactory = dbFactory;
	}
	
	public String saveList(ListSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(this.template);
			DymList dymList;
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			if(StringUtils.isBlank(req.getId())) {
				dymList = new DymList(req.getName(), req.getEnabled());
				dymList.setFieldName(req.getFieldName());
				dymList.setCreatedDateTime(date);
				dymList.setUpdatedDateTime(date);
				dymList.setCreatedBy(user.getId());	
			} else {
				dymList = template.findOne(Query.query(Criteria.where("id").is(req.getId())), DymList.class);
				dymList.setName(req.getName());
				dymList.setFieldName(req.getFieldName());
				dymList.setEnabled(req.getEnabled());
				dymList.setUpdatedDateTime(date);
				dymList.setUpdatedBy(user.getId());
			}
			
			LOG.debug("Save");
			template.save(dymList);
			
			return dymList.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String saveListDet(LisDetSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(this.template);
			DymListDet listDet;
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			if(StringUtils.isBlank(req.getId())) {
				listDet = new DymListDet(req.getCode(), req.getDesc(), req.getMeaning(), req.getEnabled());
				listDet.setCreatedDateTime(date);
				listDet.setUpdatedDateTime(date);
				listDet.setCreatedBy(user.getId());	
				listDet.setIsPrintNotice(req.getIsPrintNotice());
				listDet.setGroupId(req.getGroupId() == null ? null : new ObjectId(req.getGroupId()));
				listDet.setListId(new ObjectId(req.getDymListId()));
			} else {
				listDet = template.findOne(Query.query(Criteria.where("id").is(req.getId())), DymListDet.class);
				listDet.setCode(req.getCode());
				listDet.setDesc(req.getDesc());
				listDet.setMeaning(req.getMeaning());
				listDet.setEnabled(req.getEnabled());
				listDet.setUpdatedDateTime(date);
				listDet.setUpdatedBy(user.getId());
				listDet.setIsPrintNotice(req.getIsPrintNotice());
				listDet.setGroupId(req.getGroupId() == null ? null : new ObjectId(req.getGroupId()));
			}
			
			LOG.debug("Save");
			template.save(listDet);
			
			return listDet.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<DymList> findList(DymListFindCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Query query = Query.query(Criteria.where("enabled").in(req.getStatuses()));
			
			List<DymList> dymList = template.find(query, DymList.class);			
			
			return dymList;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<DymListDet> findListDet(DymListFindCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Query query = Query.query(Criteria.where("enabled").in(req.getStatuses()).and("listId").is(new ObjectId(req.getDymListId())));
			
			List<DymListDet> dymListDet = template.find(query, DymListDet.class);			
			
			return dymListDet;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<DymListDetGroup> findListDetGroup(DymListFindCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Query query = Query.query(Criteria.where("listId").is(new ObjectId(req.getDymListId())));
			
			List<DymListDetGroup> dymListDet = template.find(query, DymListDetGroup.class);			
			
			return dymListDet;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteList(String id, String productId) throws Exception {
		try {
			LOG.debug("Get user");			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			template.remove(Query.query(Criteria.where("listId").is(id)), DymListDet.class);
			
			template.remove(Query.query(Criteria.where("listId").is(id)), DymListDetGroup.class);
			
			template.remove(Query.query(Criteria.where("id").is(id)), DymList.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteListDet(String id, String productId) throws Exception {
		try {
			LOG.debug("Get user");			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			template.remove(Query.query(Criteria.where("id").is(id)), DymListDet.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String saveGroup(DymListDetGroupSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(this.template);
			DymListDetGroup resultCodeGroup;
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			if(StringUtils.isBlank(req.getId())) {
				resultCodeGroup = new DymListDetGroup(req.getName());
				resultCodeGroup.setCreatedDateTime(date);
				resultCodeGroup.setUpdatedDateTime(date);
				resultCodeGroup.setCreatedBy(user.getId());
				resultCodeGroup.setListId(new ObjectId(req.getDymListId()));
			} else {
				resultCodeGroup = template.findOne(Query.query(Criteria.where("id").is(req.getId())), DymListDetGroup.class);
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
			
}
