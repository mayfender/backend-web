package com.may.ple.backend.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.ForecastFindCriteriaReq;
import com.may.ple.backend.criteria.ForecastFindCriteriaResp;
import com.may.ple.backend.criteria.ForecastSaveCriteriaReq;
import com.may.ple.backend.entity.Forecast;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class ForecastService {
	private static final Logger LOG = Logger.getLogger(ForecastService.class.getName());
	private MongoTemplate template;
	private DbFactory dbFactory;
	
	@Autowired	
	public ForecastService(MongoTemplate template, DbFactory dbFactory) {
		this.template = template;
		this.dbFactory = dbFactory;
	}
	
	public void save(ForecastSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(this.template);
			Map<String, Object> forecast;
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			if(StringUtils.isBlank(req.getId())) {
				forecast = new HashMap<>();
				forecast.put("payTypeId", req.getPayTypeId());
				forecast.put("round", req.getRound());
				forecast.put("totalRound", req.getTotalRound());
				forecast.put("appointDate", req.getAppointDate());
				forecast.put("appointAmount", req.getAppointAmount());
				forecast.put("forecastPercentage", req.getForecastPercentage());
				forecast.put("paidAmount", req.getPaidAmount());
				forecast.put("comment", req.getComment());
				forecast.put("createdDateTime", date);
				forecast.put("updatedDateTime", date);
				forecast.put("createdBy", user.getId());
				forecast.put("createdByName", user.getShowname());
				forecast.put("contractNo", req.getContractNo());
			} else {
				forecast = template.findOne(Query.query(Criteria.where("_id").is(req.getId())), Map.class, "forecast");
				forecast.put("payTypeId", req.getPayTypeId());
				forecast.put("round", req.getRound());
				forecast.put("totalRound", req.getTotalRound());
				forecast.put("appointDate", req.getAppointDate());
				forecast.put("appointAmount", req.getAppointAmount());
				forecast.put("forecastPercentage", req.getForecastPercentage());
				forecast.put("paidAmount", req.getPaidAmount());
				forecast.put("comment", req.getComment());
				forecast.put("updatedDateTime", date);
				forecast.put("updatedBy", user.getId());
				forecast.put("updatedByName", user.getShowname());
			}
			
			LOG.debug("Save action code");
			template.save(forecast, "forecast");
			
			LOG.debug("Check and create Index.");
			DBCollection collection = template.getCollection("forecast");
			collection.createIndex(new BasicDBObject("payTypeId", 1));
			collection.createIndex(new BasicDBObject("round", 1));
			collection.createIndex(new BasicDBObject("totalRound", 1));
			collection.createIndex(new BasicDBObject("appointDate", 1));
			collection.createIndex(new BasicDBObject("appointAmount", 1));
			collection.createIndex(new BasicDBObject("forecastPercentage", 1));
			collection.createIndex(new BasicDBObject("paidAmount", 1));
			collection.createIndex(new BasicDBObject("createdDateTime", 1));
			collection.createIndex(new BasicDBObject("createdBy", 1));
			collection.createIndex(new BasicDBObject("contractNo", 1));
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public ForecastFindCriteriaResp find(ForecastFindCriteriaReq req) throws Exception {
		try {			
			ForecastFindCriteriaResp resp = new ForecastFindCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Criteria criteria = Criteria.where("contractNo").is(req.getContractNo());
			
			long totalItems = template.count(Query.query(criteria), Forecast.class);
			resp.setTotalItems(totalItems);
			
			if(totalItems == 0) return resp;
			
			Query query = Query.query(criteria);
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			query.with(new Sort(Direction.DESC, "createdDateTime"));
			
			query.fields()
			.include("payTypeId")
			.include("round")
			.include("totalRound")
			.include("appointDate")
			.include("appointAmount")
			.include("forecastPercentage")
			.include("paidAmount")
			.include("createdDateTime")
			.include("createdByName")
			.include("comment");
			
			List<Forecast> forecastList = template.find(query, Forecast.class);
			resp.setForecastList(forecastList);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void remove(ForecastFindCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			template.remove(Query.query(Criteria.where("id").is(req.getId())), Forecast.class);			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
