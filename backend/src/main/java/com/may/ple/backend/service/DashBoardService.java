package com.may.ple.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.criteria.DashBoardCriteriaReq;
import com.may.ple.backend.criteria.DashboardTraceCountCriteriaResp;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

@Service
public class DashBoardService {
	private static final Logger LOG = Logger.getLogger(DashBoardService.class.getName());
	private DbFactory dbFactory;
	private UserAction userAct;
	
	@Autowired	
	public DashBoardService(DbFactory dbFactory, UserAction userAct) {
		this.dbFactory = dbFactory;
		this.userAct = userAct;
	}
	
	public DashboardTraceCountCriteriaResp traceCount(DashBoardCriteriaReq req) throws Exception {
		try {			
			DashboardTraceCountCriteriaResp resp = new DashboardTraceCountCriteriaResp();
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			List<String> uIds = new ArrayList<>();
			
			for (Users u : users) { uIds.add(u.getId()); }
			
			Criteria criteria = Criteria.where("taskDetail.sys_owner_id").in(uIds);
			
			if(req.getDateFrom() != null) {
				if(req.getDateTo() != null) {
					criteria.and("createdDateTime").gte(req.getDateFrom()).lte(req.getDateTo());			
				} else {
					criteria.and("createdDateTime").gte(req.getDateFrom());
				}
			} else if(req.getDateTo() != null) {				
				criteria.and("createdDateTime").lte(req.getDateTo());
			}
			
			//--------------------: Group by first element :--------------------
			BasicDBList dbList = new BasicDBList();
			dbList.add("$taskDetail.sys_owner_id");
			dbList.add(0);
			
			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(criteria),
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$group",
					            new BasicDBObject("_id", new BasicDBObject("$arrayElemAt", dbList))
				                .append("traceNum", new BasicDBObject("$sum", 1))
					        )
						)
			);		
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			AggregationResults<Map> aggregate = template.aggregate(agg, "traceWork", Map.class);
			List<Map> mappedResults = aggregate.getMappedResults();
			List<Map> result = new ArrayList<>();
			Map mapResult;
			
			for (Users u : users) {
				mapResult = new HashMap<>();
				mapResult.put("showname", u.getShowname());
				
				if(mappedResults == null || mappedResults.size() == 0) {
					mapResult.put("traceNum", 0);
				} else {						
					mapResult.put("traceNum", 0);
					
					for (Map map : mappedResults) {		
						if(map.get("_id").equals(u.getId())) {
							mapResult.put("traceNum", map.get("traceNum"));
							break;
						}
					}
				}	
				result.add(mapResult);
			}
			
			resp.setTraceCount(result);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
			
}
