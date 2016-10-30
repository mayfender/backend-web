package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_APPOINT_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_NEXT_TIME_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;

import java.util.ArrayList;
import java.util.Calendar;
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
import com.may.ple.backend.criteria.TraceCommentCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaResp;
import com.may.ple.backend.criteria.TraceResultCriteriaReq;
import com.may.ple.backend.criteria.TraceResultCriteriaResp;
import com.may.ple.backend.criteria.TraceSaveCriteriaReq;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.ActionCode;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.ResultCode;
import com.may.ple.backend.entity.TraceWork;
import com.may.ple.backend.entity.TraceWorkComment;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.MappingUtil;
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
					if(trace.getActionCode() != null && trace.getActionCode().equals(acc.getId())) {
						trace.setActionCodeText(acc.getActCode());
						break;
					}
				}
				for (ResultCode rsc : resultCodes) {
					if(trace.getResultCode() != null && trace.getResultCode().equals(rsc.getId())) {
						trace.setResultCodeText(rsc.getRstCode());
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
				traceWork = new TraceWork(req.getResultText(), req.getTel(), req.getActionCode() == null ? null: new ObjectId(req.getActionCode()), req.getResultCode() == null ? null : new ObjectId(req.getResultCode()), req.getAppointDate(), req.getNextTimeDate());				
				traceWork.setAppointAmount(req.getAppointAmount());
				traceWork.setCreatedDateTime(date);
				traceWork.setContractNo(req.getContractNo());
				traceWork.setIdCardNo(req.getIdCardNo());
				traceWork.setCreatedBy(user.getId());		
				
				Update update = new Update();
				
				if(req.getAppointDate() != null) {
					update.set(SYS_APPOINT_DATE.getName(), req.getAppointDate());					
				}
				if(req.getNextTimeDate() != null) {
					update.set(SYS_NEXT_TIME_DATE.getName(), req.getNextTimeDate());					
				}
				
				template.updateFirst(Query.query(Criteria.where("_id").is(req.getTaskDetailId())), update, NEW_TASK_DETAIL.getName());
				template.indexOps(NEW_TASK_DETAIL.getName()).ensureIndex(new Index().on(SYS_APPOINT_DATE.getName(), Direction.ASC));
				template.indexOps(NEW_TASK_DETAIL.getName()).ensureIndex(new Index().on(SYS_NEXT_TIME_DATE.getName(), Direction.ASC));
			} else {
				traceWork = template.findOne(Query.query(Criteria.where("id").is(req.getId())), TraceWork.class);
				traceWork.setResultText(req.getResultText());
				traceWork.setTel(req.getTel());
				traceWork.setAppointAmount(req.getAppointAmount());
				traceWork.setActionCode(req.getActionCode() == null ? null : new ObjectId(req.getActionCode()));
				traceWork.setResultCode(req.getResultCode() == null ? null: new ObjectId(req.getResultCode()));
				traceWork.setAppointDate(req.getAppointDate());
				traceWork.setNextTimeDate(req.getNextTimeDate());
				traceWork.setUpdatedBy(user.getId());
				
				Query q = Query.query(Criteria.where("contractNo").is(traceWork.getContractNo()));
				q.with(new Sort(Sort.Direction.DESC, "createdDateTime"));
				TraceWork lastestTrace = template.findOne(q, TraceWork.class);
				
				if(lastestTrace.getId().equals(req.getId())) {
					LOG.info("Update " + SYS_APPOINT_DATE.getName() + " and " + SYS_NEXT_TIME_DATE.getName() + " also.");
					
					Update update = new Update();
					
					if(req.getAppointDate() != null) {
						update.set(SYS_APPOINT_DATE.getName(), req.getAppointDate());					
					}
					if(req.getNextTimeDate() != null) {
						update.set(SYS_NEXT_TIME_DATE.getName(), req.getNextTimeDate());					
					}
					
					template.updateFirst(Query.query(Criteria.where("_id").is(req.getTaskDetailId())), update, NEW_TASK_DETAIL.getName());
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
				template.updateFirst(Query.query(Criteria.where("_id").is(taskDetailId)), update, NEW_TASK_DETAIL.getName());
			}
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public TraceResultCriteriaResp traceResult(TraceResultCriteriaReq req, BasicDBObject fields) {
		try {
			TraceResultCriteriaResp resp = new TraceResultCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			
			ProductSetting productSetting = product.getProductSetting();
			
			String contactColumn = productSetting.getContractNoColumnName();
			List<ColumnFormat> headers = product.getColumnFormats();
			headers = getColumnFormatsActive(headers);
			List<Criteria> multiOrTaskDetail = new ArrayList<>();
			
			if(fields == null) {
				fields = new BasicDBObject("resultText", 1)
				.append("resultText", 1)
				.append("appointDate", 1)
				.append("appointAmount", 1)
				.append("tel", 1)
				.append("nextTimeDate", 1)
				.append("createdDateTime", 1)
				.append("link_actionCode.actCode", 1)
				.append("link_resultCode.rstCode", 1);
			}
			fields.append("link_actionCode._id", 1);
			fields.append("link_resultCode._id", 1);
			fields.append("link_address.name", 1);
			fields.append("link_address.addr1", 1);
			fields.append("link_address.addr2", 1);
			fields.append("link_address.addr3", 1);
			fields.append("link_address.addr4", 1);
			fields.append("link_address.tel", 1);
			fields.append("link_address.mobile", 1);
			
			BasicDBObject project = new BasicDBObject("$project", fields);
			fields.append("taskDetail." + SYS_OWNER_ID.getName(), 1);
			
			for (ColumnFormat columnFormat : headers) {
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
				multiOrTaskDetail.add(Criteria.where("resultText").regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));
			}
			
			Criteria criteria = new Criteria();
			
			if(!StringUtils.isBlank(req.getOwner())) {
				criteria.and("taskDetail." + SYS_OWNER_ID.getName() + ".0").is(req.getOwner());										
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
			
			if(!StringUtils.isBlank(req.getActionCodeId())) {
				criteria.and("link_actionCode.0._id").is(new ObjectId(req.getActionCodeId()));
			}
			if(!StringUtils.isBlank(req.getResultCodeId())) {
				criteria.and("link_resultCode.0._id").is(new ObjectId(req.getResultCodeId()));
			}
			
			Criteria[] multiOrArr = multiOrTaskDetail.toArray(new Criteria[multiOrTaskDetail.size()]);
			if(multiOrArr.length > 0) {
				criteria.orOperator(multiOrArr);				
			}
			
			LOG.debug("Start count");
			Aggregation aggCount = Aggregation.newAggregation(						
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$lookup",
					            new BasicDBObject("from", NEW_TASK_DETAIL.getName())
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
//					.sum("appointAmount").as("appointAmountTotal")
			);
			
			AggregationResults<Map> aggregate = template.aggregate(aggCount, TraceWork.class, Map.class);
			Map aggCountResult = aggregate.getUniqueMappedResult();
			LOG.debug("End count");
			
			LOG.debug("Get users");
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			resp.setUsers(users);
			
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
			
			LOG.debug("Start get data");
			Aggregation agg = null;
			
			if(req.getCurrentPage() != null) {
				agg = Aggregation.newAggregation(
						new CustomAggregationOperation(
						        new BasicDBObject(
						            "$lookup",
						            new BasicDBObject("from", NEW_TASK_DETAIL.getName())
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
						new CustomAggregationOperation(
						        new BasicDBObject(
						            "$lookup",
						            new BasicDBObject("from", "address")
						                .append("localField","_id")
						                .append("foreignField", "traceId")
						                .append("as", "link_address")
						        )
							),							
						new CustomAggregationOperation(project),		
						Aggregation.match(criteria),
						new CustomAggregationOperation(sort),
						Aggregation.skip((req.getCurrentPage() - 1) * req.getItemsPerPage()),
						Aggregation.limit(req.getItemsPerPage())					
					);
			} else {
				//--: For export
				agg = Aggregation.newAggregation(
						new CustomAggregationOperation(
						        new BasicDBObject(
						            "$lookup",
						            new BasicDBObject("from", NEW_TASK_DETAIL.getName())
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
						new CustomAggregationOperation(
						        new BasicDBObject(
						            "$lookup",
						            new BasicDBObject("from", "address")
						                .append("localField","_id")
						                .append("foreignField", "traceId")
						                .append("as", "link_address")
						        )
							),	
						new CustomAggregationOperation(project),		
						Aggregation.match(criteria),
						new CustomAggregationOperation(sort)					
					);
			}
	
			aggregate = template.aggregate(agg, TraceWork.class, Map.class);
			
			List<Map> result = aggregate.getMappedResults();
			List<Map<String, String>> userList;
			List<Map> taskDetails;
			List<Map> address;
			Map taskDetail;
			List<String> ownerId;
			String addrFormatStr = "";
			
			for (Map map : result) {
				taskDetails = (List<Map>)map.get("taskDetail");
				
				if(taskDetails != null && taskDetails.size() > 0) {
					taskDetail = taskDetails.get(0);
					ownerId = (List)taskDetail.get(SYS_OWNER_ID.getName());
					
					if(ownerId == null) continue;
					
					userList = MappingUtil.matchUserId(users, ownerId.get(0));
					taskDetail.put(SYS_OWNER.getName(), userList);
				}
				
				address = (List<Map>)map.get("link_address");
				
				if(address != null && address.size() > 0) {
					for (Map addr : address) {						
						addrFormatStr += ", "+addr.get("name").toString() + ": ";
						addrFormatStr += addr.get("addr1").toString() + " ";
						addrFormatStr += addr.get("addr2").toString() + " ";
						addrFormatStr += addr.get("addr3").toString() + " ";
						addrFormatStr += addr.get("addr4").toString() + " ";
						addrFormatStr += addr.get("tel").toString() + " ";
						addrFormatStr += addr.get("mobile").toString();
						addrFormatStr = addrFormatStr.trim();
					}
					
					if(addrFormatStr.length() > 0) map.put("address", addrFormatStr.substring(1).trim());
					
					map.remove("link_address");
				}
			}
			
			LOG.debug("End get data");
//			Object appointAmountTotalRaw = aggCountResult.get("appointAmountTotal");
//			Double appointAmountTotal;
//			
//			if(appointAmountTotalRaw instanceof Integer) {
//				appointAmountTotal = new Double(0);			
//			} else {
//				appointAmountTotal = (Double)appointAmountTotalRaw;
//			}
			
			resp.setTraceDatas(result);
			resp.setTotalItems(((Integer)aggCountResult.get("totalItems")).longValue());
//			resp.setAppointAmountTotal(appointAmountTotal);
			resp.setHeaders(headers);
			return resp;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void updateComment(TraceCommentCriteriaReq req) {
		try {
			LOG.debug("Start");
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = Criteria.where("contractNo").is(req.getContractNo());
			Query query = Query.query(criteria);
			
			LOG.debug("Find");
			TraceWorkComment comment = template.findOne(query, TraceWorkComment.class);
			
			Date date = Calendar.getInstance().getTime();
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			
			if(comment == null) {
				comment = new TraceWorkComment();
				comment.setCreatedDateTime(date);
				comment.setCreatedBy(user.getId());
				comment.setContractNo(req.getContractNo());
			}
			
			comment.setComment(req.getComment());
			comment.setUpdatedDateTime(date);
			comment.setUpdatedBy(user.getId());
			
			template.save(comment);
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public TraceWorkComment findComment(TraceCommentCriteriaReq req) {
		try {
			LOG.debug("Start");
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = Criteria.where("contractNo").is(req.getContractNo());
			Query query = Query.query(criteria);
			
			LOG.debug("Find");
			TraceWorkComment comment = template.findOne(query, TraceWorkComment.class);
			
			LOG.debug("End");
			return comment;
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
		
}