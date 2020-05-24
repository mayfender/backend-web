package com.may.ple.backend.service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.OrderCriteriaReq;
import com.may.ple.backend.entity.Period;

@Service
public class OrderService {
	private static final Logger LOG = Logger.getLogger(OrderService.class.getName());
	private MongoTemplate template;
	
	@Autowired	
	public OrderService(MongoTemplate template) {
		this.template = template;
	}
	
	public void savePeriod(OrderCriteriaReq req) {
		try {
			Period period = new Period();
			period.setPeriodDateTime(req.getPeriodDateTime());
			period.setCreatedDateTime(Calendar.getInstance().getTime());
			period.setUserId(new ObjectId(req.getUserId()));
			
			template.save(period, "period");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<Map> getPeriod(String userId) {
		try {
			Query query = Query.query(Criteria.where("userId").is(new ObjectId(userId)));
			query.with(new Sort(Sort.Direction.DESC, "periodDateTime"));
			
			return template.find(query, Map.class, "period");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
