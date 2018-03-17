package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.DymListDetGroupSaveCriteriaReq;
import com.may.ple.backend.criteria.DymListFindCriteriaReq;
import com.may.ple.backend.criteria.LisDetSaveCriteriaReq;
import com.may.ple.backend.criteria.ListSaveCriteriaReq;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.DymList;
import com.may.ple.backend.entity.DymListDet;
import com.may.ple.backend.entity.DymListDetGroup;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

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
				dymList.setColumnName(req.getColumnName());
				dymList.setFieldName(req.getFieldName());
				dymList.setOrder(req.getOrder());
				dymList.setCreatedDateTime(date);
				dymList.setUpdatedDateTime(date);
				dymList.setCreatedBy(user.getId());	
			} else {
				dymList = template.findOne(Query.query(Criteria.where("id").is(req.getId())), DymList.class);
				dymList.setName(req.getName());
				dymList.setColumnName(req.getColumnName());
				dymList.setFieldName(req.getFieldName());
				dymList.setOrder(req.getOrder());
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
	
	public List<Map> findFullList(DymListFindCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = Criteria.where("enabled").in(req.getStatuses());
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("order", 1));
			
			//----------------: $filter sub-array :---------------------------
			BasicDBList expLst = new BasicDBList();
			expLst.add("$$dymListDet.enabled");
			expLst.add(1);
			BasicDBObject exp = new BasicDBObject();
			exp.put("input", "$dymListDet");
			exp.put("as", "dymListDet");
			exp.put("cond", new BasicDBObject("$eq", expLst));
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
	
	public void deleteList(String id, String productId) throws Exception {
		try {
			LOG.debug("Get user");			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			template.remove(Query.query(Criteria.where("listId").is(new ObjectId(id))), DymListDet.class);
			
			template.remove(Query.query(Criteria.where("listId").is(new ObjectId(id))), DymListDetGroup.class);
			
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
	
	public void deleteGroup(String id, String productId) throws Exception {
		try {
			LOG.debug("Get user");			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			template.remove(Query.query(Criteria.where("id").is(id)), DymListDetGroup.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
			
}
