package com.may.ple.backend.service;

import java.util.Date;
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
	
	public String save(ForecastSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(this.template);
			Forecast forecast;
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			if(StringUtils.isBlank(req.getId())) {
				forecast = new Forecast();
				forecast.setPayTypeId(req.getPayTypeId());
				forecast.setRound(req.getRound());
				forecast.setTotalRound(req.getTotalRound());
				forecast.setAppointDate(req.getAppointDate());
				forecast.setAppointAmount(req.getAppointAmount());
				forecast.setForecastPercentage(req.getForecastPercentage());
				forecast.setPaidAmount(req.getPaidAmount());
				forecast.setComment(req.getComment());
				forecast.setCreatedDateTime(date);
				forecast.setUpdatedDateTime(date);
				forecast.setCreatedBy(user.getId());				
				forecast.setContractNo(req.getContractNo());
			} else {
				forecast = template.findOne(Query.query(Criteria.where("id").is(req.getId())), Forecast.class);
				forecast.setPayTypeId(req.getPayTypeId());
				forecast.setRound(req.getRound());
				forecast.setTotalRound(req.getTotalRound());
				forecast.setAppointDate(req.getAppointDate());
				forecast.setAppointAmount(req.getAppointAmount());
				forecast.setForecastPercentage(req.getForecastPercentage());
				forecast.setPaidAmount(req.getPaidAmount());
				forecast.setComment(req.getComment());
				forecast.setUpdatedDateTime(date);
				forecast.setUpdatedBy(user.getId());
			}
			
			LOG.debug("Save action code");
			template.save(forecast);
			
			return forecast.getId();
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
			.include("comment");
			
			List<Map> forecastList = template.find(Query.query(criteria), Map.class);
			resp.setForecastList(forecastList);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
