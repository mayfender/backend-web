package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.FieldSettingCriteriaReq;
import com.may.ple.backend.entity.FieldSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;

@Service
public class FieldSettingService {
	private static final Logger LOG = Logger.getLogger(FieldSettingService.class.getName());
	private MongoTemplate template;
	private DbFactory dbFactory;
	
	@Autowired	
	public FieldSettingService(MongoTemplate template, DbFactory dbFactory) {
		this.template = template;
		this.dbFactory = dbFactory;
	}
	
	public String saveList(FieldSettingCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(this.template);
			FieldSetting fieldSetting;
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			if(StringUtils.isBlank(req.getId())) {
				fieldSetting = new FieldSetting(req.getName(), req.getEnabled());
				fieldSetting.setAlias(req.getAlias());
				fieldSetting.setFunctionName(req.getFunctionName());
				fieldSetting.setOrder(1000);
				fieldSetting.setCreatedDateTime(date);
				fieldSetting.setUpdatedDateTime(date);
				fieldSetting.setCreatedBy(user.getId());
			} else {
				fieldSetting = template.findOne(Query.query(Criteria.where("id").is(req.getId())), FieldSetting.class);
				fieldSetting.setName(req.getName());
				fieldSetting.setAlias(req.getAlias());
				fieldSetting.setFunctionName(req.getFunctionName());
				fieldSetting.setEnabled(req.getEnabled());
				fieldSetting.setUpdatedDateTime(date);
				fieldSetting.setUpdatedBy(user.getId());
			}
			
			LOG.debug("Save");
			template.save(fieldSetting);
			
			return fieldSetting.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<FieldSetting> findList(FieldSettingCriteriaReq req, List<String> fields) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Query query = Query.query(Criteria.where("enabled").in(req.getStatuses()));
			query.with(new Sort("order"));
			if(fields != null) {
				for (String field : fields) {
					query.fields().include(field);					
				}
			}
			
			return template.find(query, FieldSetting.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteList(String id, String productId) throws Exception {
		try {
			LOG.debug("deleteList");			
			MongoTemplate template = dbFactory.getTemplates().get(productId);			
			template.remove(Query.query(Criteria.where("id").is(id)), FieldSetting.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateOrder(FieldSettingCriteriaReq req) throws Exception {
		try {
			LOG.debug("updateOrder");			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			List<Map> datas = req.getData();
			Update update;
			
			for (Map data : datas) {
				update = new Update();
				update.set("order", Integer.parseInt(data.get("order").toString()));
				template.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(data.get("id").toString()))), update, req.getCollectionName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	/*public String saveListDet(LisDetSaveCriteriaReq req) throws Exception {
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
				listDet.setIsSuspend(req.getIsSuspend());
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
				listDet.setIsSuspend(req.getIsSuspend());
			}
			
			LOG.debug("Save");
			template.save(listDet);
			
			return listDet.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}*/
	
	
	
	/*public List<Map> findFullList(DymListFindCriteriaReq req, boolean isAllLisDet) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = Criteria.where("enabled").in(req.getStatuses());
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("order", 1));
			
			//----------------: $filter sub-array :---------------------------
			BasicDBList expLstEnable = new BasicDBList();
			expLstEnable.add("$$dymListDet.enabled");
			expLstEnable.add(1);
			
			BasicDBList expLstDisable = new BasicDBList();
			expLstDisable.add("$$dymListDet.enabled");
			expLstDisable.add(0);
			
			BasicDBList expLstAll = new BasicDBList();
			expLstAll.add(expLstEnable);
			expLstAll.add(expLstDisable);
			
			BasicDBObject exp = new BasicDBObject();
			exp.put("input", "$dymListDet");
			exp.put("as", "dymListDet");
			
			if(isAllLisDet) {				
				exp.put("cond", new BasicDBObject("$or", expLstAll));
			} else {
				exp.put("cond", new BasicDBObject("$eq", expLstEnable));
			}
			//-------------------------------------------
			
			BasicDBObject fields = new BasicDBObject()
			.append("_id", 0)
			.append("name", 1)
			.append("columnName", 1)
			.append("fieldName", 1)
			.append("order", 1)
			.append("dymListDet", new BasicDBObject("$filter", exp))
			.append("dymListDetGroup._id", 1)
			.append("dymListDetGroup.name", 1);
			
			BasicDBObject project = new BasicDBObject("$project", fields);
			
			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(criteria),
					new CustomAggregationOperation(sort),
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$lookup",
					            new BasicDBObject("from", "dymListDet")
					                .append("localField","_id")
					                .append("foreignField", "listId")
					                .append("as", "dymListDet")
					        )
						),
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$lookup",
					            new BasicDBObject("from", "dymListDetGroup")
					                .append("localField","_id")
					                .append("foreignField", "listId")
					                .append("as", "dymListDetGroup")
					        )
						),			
					new CustomAggregationOperation(project)
				);
			
			AggregationResults<Map> aggregate = template.aggregate(agg, "dymList", Map.class);
			
			return aggregate.getMappedResults();
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
	
	public void deleteGroup(String id, String productId) throws Exception {
		try {
			LOG.debug("Get user");			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			template.remove(Query.query(Criteria.where("id").is(id)), DymListDetGroup.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}*/
			
}
