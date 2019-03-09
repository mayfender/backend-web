package com.may.ple.backend.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.DymListFindCriteriaReq;
import com.may.ple.backend.criteria.ListSaveCriteriaReq;
import com.may.ple.backend.criteria.SearchValueSaveCriteriaReq;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.DymSearch;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

@Service
public class DymSearchService {
	private static final Logger LOG = Logger.getLogger(DymSearchService.class.getName());
	private MongoTemplate template;
	private DbFactory dbFactory;
	
	@Autowired	
	public DymSearchService(MongoTemplate template, DbFactory dbFactory) {
		this.template = template;
		this.dbFactory = dbFactory;
	}
	
	public String saveField(ListSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(this.template);
			DymSearch dymSearch;
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			if(StringUtils.isBlank(req.getId())) {
				dymSearch = new DymSearch(req.getName(), req.getEnabled());
				dymSearch.setFieldName(req.getFieldName());
				dymSearch.setCreatedDateTime(date);
				dymSearch.setUpdatedDateTime(date);
				dymSearch.setCreatedBy(user.getId());	
			} else {
				dymSearch = template.findOne(Query.query(Criteria.where("id").is(req.getId())), DymSearch.class);
				dymSearch.setName(req.getName());
				dymSearch.setFieldName(req.getFieldName());
				dymSearch.setEnabled(req.getEnabled());
				dymSearch.setUpdatedDateTime(date);
				dymSearch.setUpdatedBy(user.getId());
			}
			
			LOG.debug("Save");
			template.save(dymSearch);
			
			return dymSearch.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String saveValue(SearchValueSaveCriteriaReq req) throws Exception {
		try {			
			LOG.debug("Get user");
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			DymSearch dymSearch = template.findOne(Query.query(Criteria.where("id").is(req.getFieldId())), DymSearch.class);
			Update update;
			ObjectId id;
			
			if(dymSearch.getValues() == null) {
				id = ObjectId.get();
				Map<Object, Object> value = new HashMap<>();
				value.put("id", id);
				value.put("name", req.getName());
				value.put("value", req.getValue());
				List<Map> values = new ArrayList();
				values.add(value);
				
				update = new Update();
				update.set("values", values);
				
				req.setId(String.valueOf(id));
				template.updateFirst(Query.query(Criteria.where("id").is(req.getFieldId())), update, DymSearch.class);
			} else {
				update = new Update();
				
				if(StringUtils.isBlank(req.getId())) {
					id = ObjectId.get();
					Map<Object, Object> value = new HashMap<>();
					value.put("id", id);
					value.put("name", req.getName());
					value.put("value", req.getValue());
					
					update.addToSet("values", value);
					req.setId(String.valueOf(id));
					template.updateFirst(Query.query(Criteria.where("id").is(req.getFieldId())), update, DymSearch.class);
				} else {
					update.set("values.$.name", req.getName());
					update.set("values.$.value", req.getValue());
					template.updateFirst(Query.query(Criteria.where("id").is(req.getFieldId()).and("values.id").is(new ObjectId(req.getId()))), update, DymSearch.class);
				}
			}
			
			return req.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<DymSearch> findList(String productId, List<Integer> statuses) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			Query query = Query.query(Criteria.where("enabled").in(statuses));
			
			return template.find(query, DymSearch.class);			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<Map> findFullList(DymListFindCriteriaReq req, boolean isAllLisDet) throws Exception {
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
	
	public void deleteField(String id, String productId) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			template.remove(Query.query(Criteria.where("id").is(id)), DymSearch.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteValue(String fieldId, String id, String productId) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			Update update = new Update();
			update.unset("values.$");
			template.updateFirst(Query.query(Criteria.where("id").is(fieldId).and("values.id").is(new ObjectId(id))), update, DymSearch.class);
			
			update = new Update();
			update.pull("values", null);
			
			template.updateFirst(Query.query(Criteria.where("id").is(fieldId)), update, DymSearch.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
			
}
