package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_PAYMENT_DETAIL;
import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_PROBATION_OWNER_ID;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.constant.SysFieldConstant;
import com.may.ple.backend.criteria.ForecastFindCriteriaReq;
import com.may.ple.backend.criteria.ForecastFindCriteriaResp;
import com.may.ple.backend.criteria.ForecastResultCriteriaReq;
import com.may.ple.backend.criteria.ForecastResultCriteriaResp;
import com.may.ple.backend.criteria.ForecastSaveCriteriaReq;
import com.may.ple.backend.criteria.ForecastUpdatePaidAmountCriteriaReq;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Forecast;
import com.may.ple.backend.entity.ForecastResultReportFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class ForecastService {
	private static final Logger LOG = Logger.getLogger(ForecastService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	private UserAction userAct;
	private UserService userService;
	
	@Autowired	
	public ForecastService(MongoTemplate templateCore, DbFactory dbFactory, UserAction userAct, UserService userService) {
		this.templateCore = templateCore;
		this.dbFactory = dbFactory;
		this.userAct = userAct;
		this.userService = userService;
	}
	
	public void save(ForecastSaveCriteriaReq req) throws Exception {
		try {
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			Boolean probation = user.getProbation();
			List<ColumnFormat> headers = null;
			Map<String, Object> forecast;
			boolean isCreate = false;
			Date date = new Date();
			Field fields = null;
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			
			if(StringUtils.isBlank(req.getId())) {
				isCreate = true;
				forecast = new HashMap<>();
				forecast.put("createdDateTime", date);
				forecast.put("updatedDateTime", date);
				forecast.put("createdBy", user.getId());
				forecast.put("createdByName", user.getShowname());
				forecast.put("contractNo", req.getContractNo());
				
				headers = product.getColumnFormats();
				headers = getColumnFormatsActive(headers);
				ColumnFormat columnFormatProbation = new ColumnFormat();
				columnFormatProbation.setColumnName(SYS_PROBATION_OWNER_ID.getName());
				headers.add(columnFormatProbation);
				
				String contractNoColumn = productSetting.getContractNoColumnName();
				Query query = Query.query(Criteria.where(contractNoColumn).is(req.getContractNo()));
				fields = query.fields().include(SYS_OWNER_ID.getName());
				
				for (ColumnFormat colForm : headers) {
					fields.include(colForm.getColumnName());
				}
				
				LOG.debug("Find taskDetail");
				Map taskDetail = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
				LOG.debug("Find users");
				List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
				List<String> ownerId = (List)taskDetail.get(SYS_OWNER_ID.getName());
				List<Map<String, String>> userList = MappingUtil.matchUserId(users, ownerId.get(0));
				Map u = (Map)userList.get(0);
				taskDetail.put(SYS_OWNER.getName(), u.get("showname"));
				
				if(probation != null && probation) {
					forecast.put("createdBy", ownerId.get(0));
					forecast.put("createdByName", u.get("showname"));
				}
				
				forecast.put("taskDetail", taskDetail);
			} else {
				forecast = template.findOne(Query.query(Criteria.where("_id").is(req.getId())), Map.class, "forecast");
				forecast.put("updatedDateTime", date);
				forecast.put("updatedBy", user.getId());
				forecast.put("updatedByName", user.getShowname());
			}
			
			//--: Get paidAmount From paymentDetail
			Double paidAmount = getPaidAmount(template, product, req.getContractNo(), req.getAppointDate());
			if(paidAmount != null) {
				req.setPaidAmount(paidAmount);
			}
			//--: Get paidAmount From paymentDetail
			
			forecast.put("payTypeName", req.getPayTypeName());
			forecast.put("round", req.getRound());
			forecast.put("totalRound", req.getTotalRound());
			forecast.put("appointDate", req.getAppointDate());
			forecast.put("appointAmount", req.getAppointAmount());
			forecast.put("forecastPercentage", req.getForecastPercentage());
			forecast.put("paidAmount", req.getPaidAmount());
			forecast.put("comment", req.getComment());
			
			LOG.debug("Save");
			template.save(forecast, "forecast");
			
			if(isCreate) {
				LOG.debug("Check and create Index.");
				DBCollection collection = template.getCollection("forecast");
				collection.createIndex(new BasicDBObject("payTypeName", 1));
				collection.createIndex(new BasicDBObject("round", 1));
				collection.createIndex(new BasicDBObject("totalRound", 1));
				collection.createIndex(new BasicDBObject("appointDate", 1));
				collection.createIndex(new BasicDBObject("appointAmount", 1));
				collection.createIndex(new BasicDBObject("forecastPercentage", 1));
				collection.createIndex(new BasicDBObject("paidAmount", 1));
				collection.createIndex(new BasicDBObject("createdDateTime", 1));
				collection.createIndex(new BasicDBObject("createdBy", 1));
				collection.createIndex(new BasicDBObject("contractNo", 1));
				
				for (ColumnFormat colForm : headers) {
					fields.include(colForm.getColumnName());
					collection.createIndex(new BasicDBObject("taskDetail." + colForm.getColumnName(), 1));
				}
			}
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
			.include("payTypeName")
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
	
	public ForecastResultCriteriaResp forecastResult(ForecastResultCriteriaReq req, BasicDBObject fields, boolean isNotice) throws Exception {
		try {
			ForecastResultCriteriaResp resp = new ForecastResultCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			String contactColumn = productSetting.getContractNoColumnName();
			List<ColumnFormat> headers = product.getColumnFormats();
			
			if(headers == null) return resp;
			
			headers = getColumnFormatsActive(headers);
			resp.setHeaders(headers);
			List<Criteria> multiOrTaskDetail = new ArrayList<>();
			List<Users> users = null;
			BasicDBObject sort;
			
			if(fields == null) {
				fields = new BasicDBObject()
				.append("appointAmount", 1)
				.append("appointDate", 1)
				.append("payTypeName", 1)
				.append("forecastPercentage", 1)
				.append("comment", 1)
				.append("paidAmount", 1)
				.append("paidDate", 1)
				.append("round", 1)
				.append("totalRound", 1)
				.append("createdByName", 1)
				.append("taskDetail." + SYS_OWNER.getName(), 1);
			}
			fields.append("contractNo", 1);
			fields.append("createdDateTime", 1);
			
			BasicDBObject project = new BasicDBObject("$project", fields);
			fields.append("taskDetail._id", 1);
			fields.append("taskDetail." + SYS_OWNER_ID.getName(), 1);
			fields.append("taskDetailFull._id", 1);
			
			for (ColumnFormat columnFormat : headers) {
				if(columnFormat.getColumnName().equals(SysFieldConstant.SYS_OWNER.getName())) continue;
				
				fields.append("taskDetail." + columnFormat.getColumnName(), 1);
				
				if(!StringUtils.isBlank(req.getKeyword())) {
					if(columnFormat.getDataType() != null) {
						if(columnFormat.getDataType().equals("str")) {
							multiOrTaskDetail.add(Criteria.where("taskDetail." + columnFormat.getColumnName()).regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));							
						} else if(columnFormat.getDataType().equals("num")) {
							//--: Ignore right now.
						}
					} else {
						LOG.debug(columnFormat.getColumnName() + "' dataType is null");
					}
				}
			}
			
			if(!StringUtils.isBlank(req.getKeyword())) {
				multiOrTaskDetail.add(Criteria.where("commnet").regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));
			}
			
			Criteria criteria = new Criteria();
			
			if(!StringUtils.isBlank(req.getOwner())) {
				Users user = userService.getUserById(req.getOwner());
				Boolean probation = user.getProbation();
				if(probation != null && probation) {
					criteria.and("taskDetail." + SYS_PROBATION_OWNER_ID.getName()).is(req.getOwner());
				} else {					
					criteria.and("taskDetail." + SYS_OWNER_ID.getName() + ".0").is(req.getOwner());										
				}
			}
			
			if(req.getDateFrom() != null) {
				if(req.getDateTo() != null) {
					criteria.and(req.getDateColumnName()).gte(req.getDateFrom()).lte(req.getDateTo());			
				} else {
					criteria.and(req.getDateColumnName()).gte(req.getDateFrom());
				}
			} else if(req.getDateTo() != null) {				
				criteria.and(req.getDateColumnName()).lte(req.getDateTo());
			}
						
			Criteria[] multiOrArr = multiOrTaskDetail.toArray(new Criteria[multiOrTaskDetail.size()]);
			if(multiOrArr.length > 0) {
				criteria.orOperator(multiOrArr);				
			}
			
			//-----------------------------------------------------------
			if(req.getIsInit() != null && req.getIsInit()) {
				Query queryTemplate = Query.query(Criteria.where("enabled").is(true));
				queryTemplate.with(new Sort("templateName"));
				queryTemplate.fields().include("templateName");
				List<ForecastResultReportFile> uploadTemplates = template.find(queryTemplate, ForecastResultReportFile.class);
				resp.setUploadTemplates(uploadTemplates);
			}
			
			//------------------------------------------------------------
			AggregationResults<Map> aggregate = null;
			Map aggCountResult = null;
			Aggregation aggCount = null;
			
			if(req.getCurrentPage() != null) {
				LOG.debug("Get users");
				users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
				resp.setUsers(users);
				
				LOG.debug("Start count");
				aggCount = Aggregation.newAggregation(			
						Aggregation.match(criteria),
						Aggregation.group().count().as("totalItems")	
				);
				
				aggregate = template.aggregate(aggCount, "forecast", Map.class);
				aggCountResult = aggregate.getUniqueMappedResult();
				LOG.debug("End count");
				
				if(aggCountResult == null) {
					LOG.info("Not found data");
					resp.setTotalItems(Long.valueOf(0));
					return resp;
				}
			}
			
			if(StringUtils.isBlank(req.getColumnName())) {
				sort = new BasicDBObject("$sort", new BasicDBObject("createdDateTime", -1));
			} else {
				if(req.getColumnName().equals("taskDetail." + SYS_OWNER.getName())) {
					req.setColumnName("taskDetail." + SYS_OWNER_ID.getName());
				}
				sort = new BasicDBObject("$sort", new BasicDBObject(req.getColumnName(), Direction.fromString(req.getOrder()) == Direction.ASC ? 1 : -1));
			}
			
			//-----------------------------------------------------------
			MatchOperation match = Aggregation.match(criteria);
			
			List<AggregationOperation> aggregateLst = new ArrayList<>();
			aggregateLst.add(match);
			aggregateLst.add(new CustomAggregationOperation(sort));
			
			if(req.getCurrentPage() != null) {
				aggregateLst.add(Aggregation.skip((req.getCurrentPage() - 1) * req.getItemsPerPage()));	
				aggregateLst.add(Aggregation.limit(req.getItemsPerPage()));
			} else {
				aggregateLst.add(new CustomAggregationOperation(
				        new BasicDBObject(
				            "$lookup",
				            new BasicDBObject("from", NEW_TASK_DETAIL.getName())
				                .append("localField", "contractNo")
				                .append("foreignField", contactColumn)
				                .append("as", "taskDetailFull")
				        )
					));
			}
			
			aggregateLst.add(new CustomAggregationOperation(project));
			
			aggCount = Aggregation.newAggregation(aggregateLst.toArray(new AggregationOperation[aggregateLst.size()]));			
			aggregate = template.aggregate(aggCount, "forecast", Map.class);
			List<Map> result = aggregate.getMappedResults();
			
			if(isNotice) {
				LOG.debug("return for notice");
				resp.setForecastDatas(result);
				return resp;
			}
			
			LOG.debug("End get data");
			resp.setForecastDatas(result);
			resp.setTotalItems(((Integer)aggCountResult.get("totalItems")).longValue());
			return resp;
		} catch (Exception e) {
			throw e;
		}
	}

	public Double updatePaidAmount(ForecastUpdatePaidAmountCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);			
			
			Map<String, Object> forecast = template.findOne(Query.query(Criteria.where("_id").is(req.getId())), Map.class, "forecast");
			Double paidAmount = null;
			
			if(req.getPaidDate() != null) {
				paidAmount = getPaidAmount(template, product, forecast.get("contractNo").toString(), req.getPaidDate());				
				if(paidAmount != null) {
					forecast.put("paidAmount", paidAmount);
				}
			}
			
			forecast.put("paidDate", req.getPaidDate());
			
			LOG.debug("Save");
			template.save(forecast, "forecast");
			
			return paidAmount;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats) {
		List<ColumnFormat> result = new ArrayList<>();
		
		for (ColumnFormat colFormat : columnFormats) {
			if(colFormat.getIsActive()) {
				result.add(colFormat);
			}
		}
		
		return result;
	}
	
	private Double getPaidAmount(MongoTemplate template, Product product, String contractNo, Date paidDate) {
		try {
			ProductSetting productSetting = product.getProductSetting();
			String contractNoColumnPay = productSetting.getContractNoColumnNamePayment();
			if(!StringUtils.isBlank(contractNoColumnPay)) {
				List<ColumnFormat> columnFormatsPayment = product.getColumnFormatsPayment();
				String paidDateCol = productSetting.getPaidDateColumnNamePayment();
				String paidAmountCol = null;
				
				for (ColumnFormat cp : columnFormatsPayment) {
					if(cp.getIsSum() != null && cp.getIsSum()) {
						paidAmountCol = cp.getColumnName();
						break;
					}
				}
				
				LOG.info("paidAmountCol: " + paidAmountCol);
				Query query = Query.query(Criteria.where(contractNoColumnPay).is(contractNo).and(paidDateCol).is(paidDate));
				query.fields().include(paidAmountCol);
				Map payment = template.findOne(query, Map.class, NEW_PAYMENT_DETAIL.getName());
				Double paidAmount;
				
				if(payment != null && (paidAmount = (Double)payment.get(paidAmountCol)) != null) {
					return paidAmount;
				}
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
		return null;
	}
	
}
