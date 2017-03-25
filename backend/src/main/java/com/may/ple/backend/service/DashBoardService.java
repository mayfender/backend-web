package com.may.ple.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.criteria.DashBoardCriteriaReq;
import com.may.ple.backend.criteria.DashboardPaymentCriteriaResp;
import com.may.ple.backend.criteria.DashboardTraceCountCriteriaResp;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

@Service
public class DashBoardService {
	private static final Logger LOG = Logger.getLogger(DashBoardService.class.getName());
	private MongoTemplate templateCenter;
	private DbFactory dbFactory;
	private UserAction userAct;
	
	@Autowired	
	public DashBoardService(MongoTemplate templateCenter, DbFactory dbFactory, UserAction userAct) {
		this.templateCenter = templateCenter;
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
				mapResult.put("traceNum", 0);
				
				if(!(mappedResults == null || mappedResults.size() == 0)) {
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
	
	public DashboardPaymentCriteriaResp payment(DashBoardCriteriaReq req) throws Exception {
		try {			
			DashboardPaymentCriteriaResp resp = new DashboardPaymentCriteriaResp();
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			List<String> uIds = new ArrayList<>();
			
			for (Users u : users) { uIds.add(u.getId()); }
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting setting = product.getProductSetting();
			Criteria criteria = Criteria.where("sys_owner_id").in(uIds);
			String paidDateColumn = setting.getPaidDateColumnNamePayment();
			
			if(StringUtils.isBlank(paidDateColumn)) return resp;
			
			if(req.getDateFrom() != null) {
				if(req.getDateTo() != null) {
					criteria.and(paidDateColumn).gte(req.getDateFrom()).lte(req.getDateTo());			
				} else {
					criteria.and(paidDateColumn).gte(req.getDateFrom());
				}
			} else if(req.getDateTo() != null) {				
				criteria.and(paidDateColumn).lte(req.getDateTo());
			}
			
			//----------------------------------
			List<ColumnFormat> columnFormatsPayment = product.getColumnFormatsPayment();
			List<ColumnFormat> reportCol = new ArrayList<>();
			Map<String, List<Double>> series = new HashMap<>();
			series.put("paymentNum", new ArrayList<Double>());
			
			for (ColumnFormat columnFormat : columnFormatsPayment) {
				if(columnFormat.getIsReportSum() == null || !columnFormat.getIsReportSum()) continue;
				if(StringUtils.isBlank(columnFormat.getReportSumName())) continue;
				
				reportCol.add(columnFormat);
				series.put(columnFormat.getReportSumName(), new ArrayList<Double>());
			}
			
			if(reportCol.size() == 0) {
				LOG.info("reportCol size: 0");
				return resp;
			}
			
			//----------------------------------
			GroupOperation group = Aggregation.group("sys_owner_id").count().as("paymentNum");
			for (ColumnFormat columnFormat : reportCol) {
				group = group.sum(columnFormat.getColumnName()).as(columnFormat.getReportSumName());
			}
			
			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(criteria),
					group
			);
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			AggregationResults<Map> aggregate = template.aggregate(agg, "paymentDetail", Map.class);
			List<Map> mappedResults = aggregate.getMappedResults();
			
			List<String> labels = new ArrayList<>();
			Set<Entry<String, List<Double>>> entrySet;
			
			for (Users u : users) {
				labels.add(u.getShowname());
				
				if(!(mappedResults == null || mappedResults.size() == 0)) {
					for (Map map : mappedResults) {		
						if(map.get("_id").equals(u.getId())) {
							entrySet = series.entrySet();
							
							for (Entry<String, List<Double>> entry : entrySet) {
								entry.getValue().add(Double.parseDouble(map.get(entry.getKey()).toString()));
							}
							break;
						}
					}
				} else {
					entrySet = series.entrySet();
					
					for (Entry<String, List<Double>> entry : entrySet) {
						entry.getValue().add(0.0);
					}
				}
			}
			
			resp.setDatas(series);
			resp.setLabels(labels);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
			
}
