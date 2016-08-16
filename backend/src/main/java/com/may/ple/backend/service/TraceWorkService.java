package com.may.ple.backend.service;

import static com.may.ple.backend.constant.SysFieldConstant.SYS_APPOINT_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_NEXT_TIME_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.criteria.TraceFindCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaResp;
import com.may.ple.backend.criteria.TraceResultCriteriaReq;
import com.may.ple.backend.criteria.TraceResultCriteriaResp;
import com.may.ple.backend.criteria.TraceSaveCriteriaReq;
import com.may.ple.backend.criteria.UserByProductCriteriaResp;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.ActionCode;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.ResultCode;
import com.may.ple.backend.entity.TraceWork;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.mongodb.BasicDBObject;

@Service
public class TraceWorkService {
	private static final Logger LOG = Logger.getLogger(TraceWorkService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	private UserAction userAct;
	
	@Autowired	
	public TraceWorkService(MongoTemplate template, DbFactory dbFactory, UserAction userAct) {
		this.templateCore = template;
		this.dbFactory = dbFactory;
		this.userAct = userAct;
	}
	
	public TraceFindCriteriaResp find(TraceFindCriteriaReq req) throws Exception {
		try {			
			TraceFindCriteriaResp resp = new TraceFindCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Criteria criteria = Criteria.where("contractNo").is(req.getContractNo());
			Query query = Query.query(criteria);
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			query.with(new Sort(Sort.Direction.DESC, "createdDateTime"));
			query.fields()
			.include("resultText")
			.include("tel")
			.include("actionCode")
			.include("resultCode")
			.include("appointDate")
			.include("nextTimeDate")
			.include("contractNo")
			.include("createdDateTime")
			.include("appointAmount")
			.include("createdBy");

			LOG.debug("Get total record");
			long totalItems = template.count(Query.query(criteria), TraceWork.class);
			
			LOG.debug("Find");
			List<TraceWork> traceWorks = template.find(query, TraceWork.class);			
			
			//----
			
			LOG.debug("Get actionCode");
			List<ActionCode> actionCodes = template.findAll(ActionCode.class);
			LOG.debug("Get resultCode");
			List<ResultCode> resultCodes = template.findAll(ResultCode.class);
			LOG.debug("Get users");
			List<Users> users = templateCore.find(Query.query(Criteria.where("products").in(req.getProductId())), Users.class);
			
			LOG.debug("Start merge value");
			for (TraceWork trace : traceWorks) {
				for (ActionCode acc : actionCodes) {
					if(trace.getActionCode().equals(acc.getId())) {
						trace.setActionCodeText(acc.getCode());
						break;
					}
				}
				for (ResultCode rsc : resultCodes) {
					if(trace.getResultCode().equals(rsc.getId())) {
						trace.setResultCodeText(rsc.getCode());
						break;
					}
				}
				for (Users u : users) {
					if(trace.getCreatedBy().equals(u.getId())) {
						trace.setCreatedByText(u.getShowname());
						break;
					}
				}
			}
			LOG.debug("End merge value");
			
			resp.setTraceWorks(traceWorks);
			resp.setTotalItems(totalItems);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void save(TraceSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			TraceWork traceWork;
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			if(StringUtils.isBlank(req.getId())) {
				traceWork = new TraceWork(req.getResultText(), req.getTel(), new ObjectId(req.getActionCode()), new ObjectId(req.getResultCode()), req.getAppointDate(), req.getNextTimeDate());				
				traceWork.setAppointAmount(req.getAppointAmount());
				traceWork.setCreatedDateTime(date);
				traceWork.setContractNo(req.getContractNo());
				traceWork.setCreatedBy(user.getId());		
				
				Update update = new Update();
				update.set(SYS_APPOINT_DATE.getName(), req.getAppointDate());
				update.set(SYS_NEXT_TIME_DATE.getName(), req.getNextTimeDate());
				template.updateFirst(Query.query(Criteria.where("_id").is(req.getTaskDetailId())), update, "newTaskDetail");
				
				template.indexOps("newTaskDetail").ensureIndex(new Index().on(SYS_APPOINT_DATE.getName(), Direction.ASC));
				template.indexOps("newTaskDetail").ensureIndex(new Index().on(SYS_NEXT_TIME_DATE.getName(), Direction.ASC));
			} else {
				traceWork = template.findOne(Query.query(Criteria.where("id").is(req.getId())), TraceWork.class);
				traceWork.setResultText(req.getResultText());
				traceWork.setTel(req.getTel());
				traceWork.setAppointAmount(req.getAppointAmount());
				traceWork.setActionCode(new ObjectId(req.getActionCode()));
				traceWork.setResultCode(new ObjectId(req.getResultCode()));
				traceWork.setAppointDate(req.getAppointDate());
				traceWork.setNextTimeDate(req.getNextTimeDate());
				traceWork.setUpdatedBy(user.getId());
				
				Query q = Query.query(Criteria.where("contractNo").is(traceWork.getContractNo()));
				q.with(new Sort(Sort.Direction.DESC, "createdDateTime"));
				TraceWork lastestTrace = template.findOne(q, TraceWork.class);
				
				if(lastestTrace.getId().equals(req.getId())) {
					LOG.info("Update newTaskDetail " + SYS_APPOINT_DATE.getName() + " and " + SYS_NEXT_TIME_DATE.getName() + " also.");
					
					Update update = new Update();
					update.set(SYS_APPOINT_DATE.getName(), req.getAppointDate());
					update.set(SYS_NEXT_TIME_DATE.getName(), req.getNextTimeDate());
					template.updateFirst(Query.query(Criteria.where("_id").is(req.getTaskDetailId())), update, "newTaskDetail");
				}
			}
			
			traceWork.setUpdatedDateTime(date);
			
			LOG.debug("Save");
			template.save(traceWork);
			
			template.indexOps(TraceWork.class).ensureIndex(new Index().on("createdDateTime", Direction.ASC));
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void delete(String id, String productId, String contractNo, String taskDetailId) {
		try {
			LOG.debug("Start");
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			template.remove(Query.query(Criteria.where("id").is(id)), TraceWork.class);
			
			long totalItems = template.count(Query.query(Criteria.where("contractNo").is(contractNo)), TraceWork.class);
			if(totalItems == 0) {
				Update update = new Update();
				update.set(SYS_APPOINT_DATE.getName(), null);
				update.set(SYS_NEXT_TIME_DATE.getName(), null);
				template.updateFirst(Query.query(Criteria.where("_id").is(taskDetailId)), update, "newTaskDetail");
			}
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public TraceResultCriteriaResp traceResult(TraceResultCriteriaReq req) {
		try {
			TraceResultCriteriaResp resp = new TraceResultCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			
			ProductSetting productSetting = product.getProductSetting();
			if(productSetting == null) {
				return resp;
			}
			
			String contactColumn = productSetting.getContractNoColumnName();
			List<ColumnFormat> headers = product.getColumnFormats();
			headers = getColumnFormatsActive(headers);
			List<Criteria> multiOrTaskDetail = new ArrayList<>();
			
			BasicDBObject fields = new BasicDBObject("resultText", 1)
			.append("resultText", 1)
			.append("appointDate", 1)
			.append("appointAmount", 1)
			.append("nextTimeDate", 1)
			.append("createdDateTime", 1)
			.append("link_actionCode.code", 1)
			.append("link_resultCode.code", 1);
			
			BasicDBObject project = new BasicDBObject("$project", fields);
			
			for (ColumnFormat columnFormat : headers) {
				fields.append("taskDetail." + columnFormat.getColumnName(), 1);
				
				if(!StringUtils.isBlank(req.getKeyword())) {
					if(columnFormat.getDataType() != null) {
						if(columnFormat.getDataType().equals("str")) {
							multiOrTaskDetail.add(Criteria.where("taskDetail\\." + columnFormat.getColumnName()).regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));							
						} else if(columnFormat.getDataType().equals("num")) {
							//--: Ignore right now.
						}
					} else {
						LOG.debug(columnFormat.getColumnName() + "' dataType is null");
					}
				}
			}
			
			if(!StringUtils.isBlank(req.getKeyword())) {
				multiOrTaskDetail.add(Criteria.where("resultText").regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));
			}
			
			Criteria criteria = new Criteria();
			
			if(!StringUtils.isBlank(req.getOwner())) {
				criteria.and("taskDetail." + SYS_OWNER.getName() + ".0.username").is(req.getOwner());										
			}
			
			Criteria[] multiOrArr = multiOrTaskDetail.toArray(new Criteria[multiOrTaskDetail.size()]);
			if(multiOrArr.length > 0) {
				criteria.orOperator(multiOrArr);				
			}
			
			Aggregation aggCount = Aggregation.newAggregation(						
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$lookup",
					            new BasicDBObject("from", "newTaskDetail")
					                .append("localField","contractNo")
					                .append("foreignField", contactColumn)
					                .append("as", "taskDetail")
					        )
						),
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$lookup",
					            new BasicDBObject("from", "actionCode")
					                .append("localField","actionCode")
					                .append("foreignField", "_id")
					                .append("as", "link_actionCode")
					        )
						),
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$lookup",
					            new BasicDBObject("from", "resultCode")
					                .append("localField","resultCode")
					                .append("foreignField", "_id")
					                .append("as", "link_resultCode")
					        )
						),
					Aggregation.match(criteria),
					Aggregation.group().count().as("totalItems")
			);
			
			AggregationResults<Map> aggregate = template.aggregate(aggCount, TraceWork.class, Map.class);
			Map aggCountResult = aggregate.getUniqueMappedResult();
			
			if(aggCountResult == null) {
				LOG.info("Not found data");
				resp.setTotalItems(Long.valueOf(0));
				return resp;
			}
			
			BasicDBObject sort;
			
			if(StringUtils.isBlank(req.getColumnName())) {
				sort = new BasicDBObject("$sort", new BasicDBObject("createdDateTime", -1));
			} else {
				sort = new BasicDBObject("$sort", new BasicDBObject(req.getColumnName(), Direction.fromString(req.getOrder()) == Direction.ASC ? 1 : -1));
			}
			
			Aggregation agg = Aggregation.newAggregation(
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$lookup",
					            new BasicDBObject("from", "newTaskDetail")
					                .append("localField","contractNo")
					                .append("foreignField", contactColumn)
					                .append("as", "taskDetail")
					        )
						),
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$lookup",
					            new BasicDBObject("from", "actionCode")
					                .append("localField","actionCode")
					                .append("foreignField", "_id")
					                .append("as", "link_actionCode")
					        )
						),
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$lookup",
					            new BasicDBObject("from", "resultCode")
					                .append("localField","resultCode")
					                .append("foreignField", "_id")
					                .append("as", "link_resultCode")
					        )
						),					
					new CustomAggregationOperation(project),		
					Aggregation.match(criteria),
					new CustomAggregationOperation(sort),
					Aggregation.skip((req.getCurrentPage() - 1) * req.getItemsPerPage()),
					Aggregation.limit(req.getItemsPerPage())					
				);
	
			aggregate = template.aggregate(agg, TraceWork.class, Map.class);
			List<Map> result = aggregate.getMappedResults();
			
			UserByProductCriteriaResp userResp = userAct.getUserByProductToAssign(req.getProductId());
	
			resp.setUsers(userResp.getUsers());
			resp.setTraceDatas(result);
			resp.setTotalItems(((Integer)aggCountResult.get("totalItems")).longValue());
			resp.setHeaders(headers);
			return resp;
		} catch (Exception e) {
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
		
}