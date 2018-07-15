package com.may.ple.backend.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.NotificationCriteriaReq;
import com.may.ple.backend.criteria.NotificationCriteriaResp;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class NotificationService {
	private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	private UserService uService;
	
	@Autowired	
	public NotificationService(MongoTemplate templateCore, DbFactory dbFactory, UserService uService) {
		this.templateCore = templateCore;
		this.dbFactory = dbFactory;
		this.uService = uService;
	}
	
	public void traceBooking(Date appointDate, Date nextTimeDate, String contractNo, String productId, String detail, String userId) {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			List<Integer> groups = new ArrayList<>();
			groups.add(1);
			groups.add(2);
			
			Criteria criteria = Criteria.where("contractNo").is(contractNo).and("group").in(groups);
			Query query = Query.query(criteria);
			query.fields().include("group");
			
			List<Map> notifications = template.find(query, Map.class, "notification");
			Map group1 = null, group2 = null;
			for (Map group : notifications) {
				if((int)group.get("group") == 1) {
					group1 = group;
				} else {
					group2 = group;
				}
			}
			
			NotificationCriteriaReq req = new NotificationCriteriaReq();
			req.setProductId(productId);
			req.setDetail(detail);
			req.setContractNo(contractNo);
			req.setUserId(userId);
			
			if(appointDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(appointDate);
				cal.set(Calendar.HOUR_OF_DAY, 0);  
				cal.set(Calendar.MINUTE, 0);  
				cal.set(Calendar.SECOND, 0);  
				cal.set(Calendar.MILLISECOND, 0); 
				
				req.setSubject("นัดชำระ");
				req.setBookingDateTime(cal.getTime());
				req.setGroup(1);
				
				if(group1 != null) {
					req.setId(group1.get("_id").toString());
				}
				booking(req);
			} else if(group1 != null) {
				req.setId(group1.get("_id").toString());
				remove(req);
			}
			
			if(nextTimeDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(nextTimeDate);
				cal.set(Calendar.HOUR_OF_DAY, 0);  
				cal.set(Calendar.MINUTE, 0);  
				cal.set(Calendar.SECOND, 0);  
				cal.set(Calendar.MILLISECOND, 0); 
				
				req.setSubject("นัด Call");
				req.setBookingDateTime(cal.getTime());
				req.setGroup(2);
				
				if(group2 != null) {
					req.setId(group2.get("_id").toString());
				}
				booking(req);
			} else if(group2 != null) {
				req.setId(group2.get("_id").toString());
				remove(req);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void booking(NotificationCriteriaReq req) {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Date now = Calendar.getInstance().getTime();
			
			if(req.getId() == null) {
				Map booking = new HashMap<>();
				booking.put("subject", req.getSubject());
				booking.put("detail", req.getDetail());
				booking.put("group", req.getGroup());
				booking.put("isTakeAction", false);
				booking.put("user_id", new ObjectId(req.getUserId()));
				booking.put("bookingDateTime", req.getBookingDateTime());
				booking.put("createdDateTime", now);
				
				if(!StringUtils.isBlank(req.getContractNo())) {
					booking.put("contractNo", req.getContractNo());
				}
				
				template.save(booking, "notification");
				
				DBCollection collection = template.getCollection("notification");
				collection.createIndex(new BasicDBObject("subject", 1));
				collection.createIndex(new BasicDBObject("group", 1));
				collection.createIndex(new BasicDBObject("isTakeAction", 1));
				collection.createIndex(new BasicDBObject("createdDateTime", 1));
				collection.createIndex(new BasicDBObject("bookingDateTime", 1));
				collection.createIndex(new BasicDBObject("user_id", 1));
			} else {
				Update update = new Update();
				update.set("subject", req.getSubject());
				update.set("detail", req.getDetail());
				update.set("bookingDateTime", req.getBookingDateTime());
				update.set("updatedDateTime", now);
				
				template.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(req.getId()))), update, "notification");
			}
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

			List<String> roles = new ArrayList<>();
			roles.add("ROLE_USER");
			roles.add("ROLE_SUPERVISOR");
			List<Users> users = uService.getUser(req.getProductId(), roles);
			resp.setUsers(users);
			
			LOG.debug("Get alert amount");
			List<Map> alertNum = getAlertNum(now, req.getProductId(), req.getUserId());
			resp.setGroupAlertNum(alertNum);
			
			LOG.debug("Get by group");
			Criteria criteria = new Criteria();
			
			if(!StringUtils.isBlank(req.getUserId())) {
				criteria.and("user_id").is(new ObjectId(req.getUserId()));
			}
			
			if(req.getActionCode().intValue() == 4) {
				criteria.and("group").is(req.getGroup()).and("bookingDateTime").gt(now);
			} else {				
				criteria.and("group").is(req.getGroup()).and("bookingDateTime").lte(now);
				if(req.getActionCode().intValue() == 1) {
					criteria.and("isTakeAction").is(false);
				} else if(req.getActionCode().intValue() == 2) {
					criteria.and("isTakeAction").is(true);
				}
			}
			
			long totalItems = template.count(Query.query(criteria), "notification");
			resp.setTotalItems(totalItems);
			
			if(totalItems == 0) return resp;
			
			Query query = Query.query(criteria);
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			
			if(req.getActionCode().intValue() == 4) {
				query.with(new Sort(Direction.DESC, "createdDateTime"));				
			} else {
				query.with(new Sort(Direction.DESC, "bookingDateTime"));
			}
			
			List<Map> notifications = template.find(query, Map.class, "notification");
			Date today = Calendar.getInstance().getTime();
			Date date;
			
			for (Map map : notifications) {
				date = ((Date)map.get("bookingDateTime"));
				
				if((int)map.get("group") < 3) {
					if(DateUtils.isSameDay(date, today)) {						
						map.put("bookingDateTimeStr", "วันนี้");
					} else {						
						map.put("bookingDateTimeStr", String.format("%1$td/%1$tm/%1$tY", date));
					}
				} else {
					if(DateUtils.isSameDay(date, today)) {
						map.put("bookingDateTimeStr", String.format("วันนี้ เวลา %1$tH:%1$tM", date));
					} else {
						map.put("bookingDateTimeStr", String.format("%1$td/%1$tm/%1$tY %1$tH:%1$tM", date));
					}
				}
			}
			
			resp.setNotificationList(notifications);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<Map> getAlertNum(Date now, String productId, String userId) {
		try {
			Criteria criteria = Criteria.where("bookingDateTime").lte(now).and("isTakeAction").is(false);
			
			if(!StringUtils.isBlank(userId)) {
				criteria.and("user_id").is(new ObjectId(userId));				
			}
			
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
	
	public List<Map> getAlertNumOverall() throws Exception {
		try {
			Date now = Calendar.getInstance().getTime();
			List<Product> prds = templateCore.find(Query.query(Criteria.where("productSetting.isHideAlert").ne(true)), Product.class);
			List<Users> lUsers = uService.getUser(null, null);
			Map<String, String> mUsers = new HashMap<>();
			for (Users u : lUsers) {
				mUsers.put(u.getId(), u.getUsername());
			}
			
			List<Map> lResult = new ArrayList<>();
			AggregationResults<Map> aggregate;
			MongoTemplate template;
			Criteria criteria;
			List<Map> result;
			Aggregation agg;
			for (Product prd : prds) {
				criteria = Criteria.where("bookingDateTime").lte(now).and("isTakeAction").is(false);
			
				agg = Aggregation.newAggregation(
						Aggregation.match(criteria),
						new CustomAggregationOperation(
					        new BasicDBObject(
					            "$group",
					            new BasicDBObject("_id", "$user_id").append("alertNum", new BasicDBObject("$sum", 1))
					        )
						)
				);
				
				template = dbFactory.getTemplates().get(prd.getId());
				aggregate = template.aggregate(agg, "notification", Map.class);
				result = aggregate.getMappedResults();
				
				for (Map map : result) {
					if(!mUsers.containsKey(map.get("_id").toString())) continue;
					map.put(mUsers.get(map.get("_id").toString()), map.get("alertNum"));
					map.remove("_id");
					map.remove("alertNum");
				}
				lResult.addAll(result);
			}
			
			return lResult;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void takeAction(NotificationCriteriaReq req) {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Date now = Calendar.getInstance().getTime();
			
			Update update = new Update();
			update.set("isTakeAction", req.getIsTakeAction());
			update.set("updatedDateTime", now);
			
			template.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(req.getId()))), update, "notification");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void remove(NotificationCriteriaReq req) {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			template.remove(Query.query(Criteria.where("_id").is(new ObjectId(req.getId()))), "notification");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
