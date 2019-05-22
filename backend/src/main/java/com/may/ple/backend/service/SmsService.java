package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_PAYMENT_DETAIL;
import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_IS_ACTIVE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_PROBATION_OWNER_ID;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.constant.SysFieldConstant;
import com.may.ple.backend.criteria.DymListFindCriteriaReq;
import com.may.ple.backend.criteria.ForecastResultCriteriaReq;
import com.may.ple.backend.criteria.ForecastResultCriteriaResp;
import com.may.ple.backend.criteria.ForecastUpdatePaidAmountCriteriaReq;
import com.may.ple.backend.criteria.SmsCriteriaReq;
import com.may.ple.backend.criteria.SmsCriteriaResp;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Forecast;
import com.may.ple.backend.entity.ForecastResultReportFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class SmsService {
	private static final Logger LOG = Logger.getLogger(SmsService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	private UserAction userAct;
	private UserService userService;
	private DymListService dymService;
	private DymSearchService dymSearchService;
	
	@Autowired	
	public SmsService(MongoTemplate templateCore, DbFactory dbFactory, UserAction userAct, UserService userService,
						   DymListService dymService, DymSearchService dymSearchService) {
		this.templateCore = templateCore;
		this.dbFactory = dbFactory;
		this.userAct = userAct;
		this.userService = userService;
		this.dymService = dymService;
		this.dymSearchService = dymSearchService;
	}
	
	public void save(SmsCriteriaReq req) throws Exception {
		try {
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			List<ObjectId> ids = new ArrayList<>();
			
			for (String id : req.getIds()) {
				ids.add(new ObjectId(id));
			}
			
			Query query = Query.query(Criteria.where("_id").in(ids));
			Field fields = query.fields().include(SYS_OWNER_ID.getName());
			List<ColumnFormat> headers = product.getColumnFormats();
			headers = getColumnFormatsActive(headers);
			
			for (ColumnFormat colForm : headers) {
				fields.include(colForm.getColumnName());
			}
			
			List<Map> taskDetails = template.find(query, Map.class, NEW_TASK_DETAIL.getName());
			Calendar now = Calendar.getInstance();
			Update update;
			for (Map taskDetail : taskDetails) {
				update = new Update();
				update.set("taskDetail", taskDetail);
				update.set("status", 0);
				update.set("createdDateTime", now.getTime());
				update.set("createdBy", new ObjectId(user.getId()));
				update.set("messageField", req.getMessageField());
				update.set("message", "");
				
				query = Query.query(Criteria.where("taskDetail." + productSetting.getContractNoColumnName())
						.is(taskDetail.get(productSetting.getContractNoColumnName()))
						.and("status").is(0));
						
				template.upsert(query, update, "sms");
			}
			
			LOG.debug("Check and create Index.");
			DBCollection collection = template.getCollection("sms");
			collection.createIndex(new BasicDBObject("status", 1));
			collection.createIndex(new BasicDBObject("createdBy", 1));
			collection.createIndex(new BasicDBObject("messageField", 1));
			
			for (ColumnFormat colForm : headers) {
				fields.include(colForm.getColumnName());
				collection.createIndex(new BasicDBObject("taskDetail." + colForm.getColumnName(), 1));
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public SmsCriteriaResp get(SmsCriteriaReq req) throws Exception {
		try {			
			SmsCriteriaResp resp = new SmsCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

//			Criteria criteria = Criteria.where("status").is(req.getStatus());
			Criteria criteria = new Criteria();
			
			long totalItems = template.count(Query.query(criteria), "sms");
			resp.setTotalItems(totalItems);
			
			if(totalItems == 0) return resp;
			
			Query query = Query.query(criteria);
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			query.with(new Sort(Direction.DESC, "createdDateTime"));
			
			/*query.fields()
			.include("status")
			.include("createdDateTime")
			.include("createdByName")
			.include("taskDetail.sys_owner");*/
			
			List<Map> smses = template.find(query, Map.class, "sms");
			resp.setSmses(smses);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void remove(SmsCriteriaReq req) throws Exception {
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
			resp.setCreatedByLog(productSetting.getCreatedByLog());
			
			if(headers == null) return resp;
			
			headers = getColumnFormatsActive(headers);
			resp.setHeaders(headers);
			List<Criteria> multiOrTaskDetail = new ArrayList<>();
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			List<String> probationUserIds = new ArrayList<>();
			BasicDBObject sort;
			
			for (Users u : users) {
				if(u.getProbation() == null || !u.getProbation()) continue;
				probationUserIds.add(u.getId());
			}
			
			LOG.debug("dymList");
			List<Integer> statuses = new ArrayList<>();
			statuses.add(1);
			DymListFindCriteriaReq reqDym = new DymListFindCriteriaReq();
			reqDym.setStatuses(statuses);
			reqDym.setProductId(req.getProductId());
			resp.setDymList(dymService.findFullList(reqDym, false));
			resp.setDymSearch(dymSearchService.getFields(req.getProductId(), statuses));
			
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
			fields.append("taskDetailFull." + SYS_IS_ACTIVE.getName(), 1);
			
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
					if(probationUserIds.size() > 0) {
						criteria.and("taskDetail." + SYS_PROBATION_OWNER_ID.getName()).nin(probationUserIds);
					}
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
						
			if(!StringUtils.isBlank(req.getCodeValue())) {
				Query queryCode = Query.query(Criteria.where(SYS_IS_ACTIVE.getName() + ".status").is(true).and(req.getCodeName()).is(new ObjectId(req.getCodeValue())));
				queryCode.fields().include(contactColumn).exclude("_id");
				List<Map> taskDetails = template.find(queryCode, Map.class, NEW_TASK_DETAIL.getName());
				if(taskDetails != null) {
					List<String> contractNos = new ArrayList<>();
					for (Map map : taskDetails) {
						contractNos.add(map.get(contactColumn).toString());
					}
					if(contractNos.size() > 0) {
						criteria.and("contractNo").in(contractNos);
					} else {
						return resp;
					}
				}
			}
			
			if(!StringUtils.isBlank(req.getDymSearchFiedVal())) {
				criteria.and("taskDetail." + req.getDymSearchFiedName()).is(req.getDymSearchFiedVal());
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
