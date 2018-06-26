package com.may.ple.backend.service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.NotificationCriteriaReq;
import com.may.ple.backend.criteria.NotificationCriteriaResp;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class NotificationService {
	private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	
	@Autowired	
	public NotificationService(MongoTemplate templateCore, DbFactory dbFactory) {
		this.templateCore = templateCore;
		this.dbFactory = dbFactory;
	}
	
	public void booking(NotificationCriteriaReq req) {
		try {
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Map booking = new HashMap<>();
			booking.put("subject", req.getSubject());
			booking.put("detail", req.getDetail());
			booking.put("group", req.getGroup());
			booking.put("isTakeAction", false);
			booking.put("bookingDateTime", req.getBookingDateTime());
			booking.put("user_id", new ObjectId(user.getId()));
			
			template.save(booking, "notification");
			
			DBCollection collection = template.getCollection("notification");
			collection.createIndex(new BasicDBObject("subject", 1));
			collection.createIndex(new BasicDBObject("group", 1));
			collection.createIndex(new BasicDBObject("isTakeAction", 1));
			collection.createIndex(new BasicDBObject("bookingDateTime", 1));
			collection.createIndex(new BasicDBObject("user_id", 1));
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public NotificationCriteriaResp getAlert(NotificationCriteriaReq req) throws Exception {
		try {			
			NotificationCriteriaResp resp = new NotificationCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Date now = Calendar.getInstance().getTime();

			LOG.debug("Get alert amount");
			List<Map> alertNum = getAlertNum(now, req.getProductId());
			resp.setGroupAlertNum(alertNum);
			
			LOG.debug("Get by group");
			Criteria criteria = Criteria.where("group").is(req.getGroup()).and("bookingDateTime").lte(now);
			if(req.getIsTakeAction() != null) {
				criteria.and("isTakeAction").is(req.getIsTakeAction());
			}
			
			long totalItems = template.count(Query.query(criteria), "notification");
			resp.setTotalItems(totalItems);
			
			if(totalItems == 0) return resp;
			
			Query query = Query.query(criteria);
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			query.with(new Sort(Direction.DESC, "bookingDateTime"));
			
			List<Map> notifications = template.find(query, Map.class, "notification");
			Date today = Calendar.getInstance().getTime();
			Date date;
			
			for (Map map : notifications) {
				date = ((Date)map.get("bookingDateTime"));
				
				if(DateUtils.isSameDay(date, today)) {
					map.put("bookingDateTime", String.format("%1$tH:%1$tM", date));
				} else {
					map.put("bookingDateTime", String.format("%1$td/%1$tm/%1$tY %1$tH:%1$tM", date));
				}
			}
			
			resp.setNotificationList(notifications);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<Map> getAlertNum(Date now, String productId) {
		try {
			Criteria criteria = Criteria.where("bookingDateTime").lte(now).and("isTakeAction").is(false);
			
			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(criteria),
					new CustomAggregationOperation(
				        new BasicDBObject(
				            "$group",
				            new BasicDBObject("_id", "$group").append("alertNum", new BasicDBObject("$sum", 1))
				        )
					)
			);
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			AggregationResults<Map> aggregate = template.aggregate(agg, "notification", Map.class);
			return aggregate.getMappedResults();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
