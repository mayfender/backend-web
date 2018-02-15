package com.may.ple.backend.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.StampCriteriaReq;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class AccessManagementService {
	private static final Logger LOG = Logger.getLogger(AccessManagementService.class.getName());
	private MongoTemplate coreTemplate;
	
	@Autowired	
	public AccessManagementService(MongoTemplate template) {
		this.coreTemplate = template;
	}
	
	/*public List<ActionCode> findActionCode(ActionCodeFindCriteriaReq req) throws Exception {
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
	}*/
	
	
	public void saveRestTimeOut(StampCriteriaReq req) throws Exception {
		try {
			Calendar time = Calendar.getInstance();
			Query query = Query.query(Criteria.where("deviceId").is(req.getDeviceId()).and("userId").is(new ObjectId(req.getUserId())).and("isActive").is(true));
			
			Update update = new Update();
			update.set("isActive", false);
			update.set("endTime", time.getTime());
			coreTemplate.updateFirst(query, update, "restTimeOut");
			
			if(req.getAction().equals("start")) {
				Map<String, Object> data = new HashMap<>();
				data.put("createdDateTime", time.getTime());
				data.put("timeLimited", req.getTimeLimited());
				data.put("isActive", true);
				data.put("productId", new ObjectId(req.getProductId()));
				data.put("userId", new ObjectId(req.getUserId()));
				data.put("deviceId", req.getDeviceId());
				
				time.add(Calendar.MINUTE, -req.getTimeLimited());
				data.put("startTime", time.getTime());
				
				coreTemplate.save(data, "restTimeOut");
				
				DBCollection collection = coreTemplate.getCollection("restTimeOut");
				collection.createIndex(new BasicDBObject("deviceId", 1));
				collection.createIndex(new BasicDBObject("userId", 1));
				collection.createIndex(new BasicDBObject("isActive", 1));
				collection.createIndex(new BasicDBObject("createdDateTime", 1));
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
