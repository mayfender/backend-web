package com.may.ple.backend.service;

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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.ibm.icu.util.Calendar;
import com.may.ple.backend.criteria.NotificationCriteriaReq;
import com.may.ple.backend.criteria.NotificationCriteriaResp;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;

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
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public NotificationCriteriaResp get(NotificationCriteriaReq req) throws Exception {
		try {			
			NotificationCriteriaResp resp = new NotificationCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Criteria criteria = Criteria.where("group").is(req.getGroup());
			if(req.getIsTakeAction() != null) {
				criteria.and("isTakeAction").is(req.getIsTakeAction());
			}
			
			long totalItems = template.count(Query.query(criteria), "notification");
			resp.setTotalItems(totalItems);
			
			if(totalItems == 0) return resp;
			
			Query query = Query.query(criteria);
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			query.with(new Sort(Direction.ASC, "bookingDateTime"));
			
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
	
}
