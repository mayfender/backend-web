package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_APPOINT_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_NEXT_TIME_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_TRACE_DATE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.constant.ActionConstant;
import com.may.ple.backend.criteria.NoticeDownloadCriteriaResp;
import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.TraceCommentCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaResp;
import com.may.ple.backend.criteria.TraceResultCriteriaReq;
import com.may.ple.backend.criteria.TraceResultCriteriaResp;
import com.may.ple.backend.criteria.TraceSaveCriteriaReq;
import com.may.ple.backend.criteria.UpdateTraceResultCriteriaReq;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.ActionCode;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.ResultCode;
import com.may.ple.backend.entity.TraceWork;
import com.may.ple.backend.entity.TraceWorkComment;
import com.may.ple.backend.entity.TraceWorkUpdatedHistory;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.IsHoldModel;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.mongodb.BasicDBObject;

@Service
public class TraceWorkService {
	private static final Logger LOG = Logger.getLogger(TraceWorkService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	private UserAction userAct;
	private NoticeUploadService noticeUploadService;
	
	@Autowired	
	public TraceWorkService(MongoTemplate template, DbFactory dbFactory, UserAction userAct,
			NoticeUploadService noticeUploadService) {
		this.templateCore = template;
		this.dbFactory = dbFactory;
		this.userAct = userAct;
		this.noticeUploadService = noticeUploadService;
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
			.include("createdBy")
			.include("templateId")
			.include("addressNotice");

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
			Date dummyDate = new Date(Long.MAX_VALUE);
			
			if(StringUtils.isBlank(req.getId())) {
				traceWork = new TraceWork(req.getResultText(), req.getTel(), req.getActionCode() == null ? null: new ObjectId(req.getActionCode()), req.getResultCode() == null ? null : new ObjectId(req.getResultCode()), req.getAppointDate(), req.getNextTimeDate());				
				traceWork.setAppointAmount(req.getAppointAmount());
				traceWork.setCreatedDateTime(date);
				traceWork.setContractNo(req.getContractNo());
				traceWork.setIdCardNo(req.getIdCardNo());
				traceWork.setCreatedBy(user.getId());		
				traceWork.setTemplateId(req.getTemplateId() == null ? null: new ObjectId(req.getTemplateId()));
				traceWork.setAddressNotice(req.getAddressNotice());
				traceWork.setAddressNoticeStr(req.getAddressNoticeStr());
				
				Update update = new Update();
				update.set(SYS_TRACE_DATE.getName(), date);
				
				if(req.getAppointDate() != null) {
					update.set(SYS_APPOINT_DATE.getName(), req.getAppointDate());		
					
					if(req.getNextTimeDate() == null) {
						update.set(SYS_NEXT_TIME_DATE.getName(), dummyDate);
					}
				}
				if(req.getNextTimeDate() != null) {
					update.set(SYS_NEXT_TIME_DATE.getName(), req.getNextTimeDate());	
					
					if(req.getAppointDate() == null) {
						update.set(SYS_APPOINT_DATE.getName(), dummyDate);
					}
				}
				
				template.updateFirst(Query.query(Criteria.where("_id").is(req.getTaskDetailId())), update, NEW_TASK_DETAIL.getName());
				
				//--: Response
				req.setTraceDate(date);
			} else {
				traceWork = template.findOne(Query.query(Criteria.where("id").is(req.getId())), TraceWork.class);
				
				//---: Save updated trace data history
				LOG.info("Save updated data as history");
				TraceWorkUpdatedHistory traceHis = new TraceWorkUpdatedHistory();
				BeanUtils.copyProperties(traceHis, traceWork);
				traceHis.setId(null);
				traceHis.setCreatedDateTime(date);
				traceHis.setTraceWorkId(new ObjectId(traceWork.getId()));
				traceHis.setAction(ActionConstant.UPDATED.getName());
				template.save(traceHis);
				template.indexOps(TraceWorkUpdatedHistory.class).ensureIndex(new Index().on("createdDateTime", Direction.ASC));
				template.indexOps(TraceWorkUpdatedHistory.class).ensureIndex(new Index().on("traceWorkId", Direction.ASC));
				
				//---:
				traceWork.setResultText(req.getResultText());
				traceWork.setTel(req.getTel());
				traceWork.setAppointAmount(req.getAppointAmount());
				traceWork.setActionCode(req.getActionCode() == null ? null : new ObjectId(req.getActionCode()));
				traceWork.setResultCode(req.getResultCode() == null ? null: new ObjectId(req.getResultCode()));
				traceWork.setAppointDate(req.getAppointDate());
				traceWork.setNextTimeDate(req.getNextTimeDate());
				traceWork.setUpdatedBy(user.getId());
				traceWork.setTemplateId(req.getTemplateId() == null ? null: new ObjectId(req.getTemplateId()));
				traceWork.setAddressNotice(req.getAddressNotice());
				traceWork.setAddressNoticeStr(req.getAddressNoticeStr());
				
				Query q = Query.query(Criteria.where("contractNo").is(traceWork.getContractNo()));
				q.with(new Sort(Sort.Direction.DESC, "createdDateTime"));
				TraceWork lastestTrace = template.findOne(q, TraceWork.class);
				
				if(lastestTrace.getId().equals(req.getId())) {
					LOG.info("Update " + SYS_APPOINT_DATE.getName() + " and " + SYS_NEXT_TIME_DATE.getName() + " also.");
					
					Update update = new Update();
					update.set(SYS_APPOINT_DATE.getName(), req.getAppointDate() == null ? dummyDate : req.getAppointDate());					
					update.set(SYS_NEXT_TIME_DATE.getName(), req.getNextTimeDate() == null ? dummyDate : req.getNextTimeDate());
					
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
	
	public void delete(String id, String productId, String contractNo, String taskDetailId) throws Exception {
		try {
			LOG.debug("Start");
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			Date date = new Date();
			Date dummyDate = new Date(Long.MAX_VALUE);
			Query query = Query.query(Criteria.where("id").is(id));
			
			TraceWork traceWork = template.findOne(query, TraceWork.class);
			
			//---: Save deleted trace data history
			LOG.info("Save updated data as history");
			TraceWorkUpdatedHistory traceHis = new TraceWorkUpdatedHistory();
			BeanUtils.copyProperties(traceHis, traceWork);
			traceHis.setId(null);
			traceHis.setCreatedDateTime(date);
			traceHis.setTraceWorkId(new ObjectId(traceWork.getId()));
			traceHis.setAction(ActionConstant.DELETED.getName());
			template.save(traceHis);
			
			//---:
			template.remove(query, TraceWork.class);
			
			long totalItems = template.count(Query.query(Criteria.where("contractNo").is(contractNo)), TraceWork.class);
			if(totalItems == 0) {
				Update update = new Update();
				update.set(SYS_APPOINT_DATE.getName(), dummyDate);
				update.set(SYS_NEXT_TIME_DATE.getName(), dummyDate);
				update.set(SYS_TRACE_DATE.getName(), dummyDate);
				template.updateFirst(Query.query(Criteria.where("_id").is(taskDetailId)), update, NEW_TASK_DETAIL.getName());
			}
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public TraceResultCriteriaResp traceResult(TraceResultCriteriaReq req, BasicDBObject fields, boolean isNotice) {
		try {
			TraceResultCriteriaResp resp = new TraceResultCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			
			ProductSetting productSetting = product.getProductSetting();
			
			if(productSetting.getIsDisableNoticePrint() == null) {
				resp.setIsDisableNoticePrint(false);				
			} else {
				resp.setIsDisableNoticePrint(productSetting.getIsDisableNoticePrint());								
			}
			
			resp.setIsTraceExportExcel(productSetting.getIsTraceExportExcel());
			resp.setIsTraceExportTxt(productSetting.getIsTraceExportTxt());
			
			String contactColumn = productSetting.getContractNoColumnName();
			List<ColumnFormat> headers = product.getColumnFormats();
			if(headers == null) return resp;
			
			headers = getColumnFormatsActive(headers);
			resp.setHeaders(headers);
			List<Criteria> multiOrTaskDetail = new ArrayList<>();
			List<Users> users = null;
			
			if(fields == null) {
				fields = new BasicDBObject()
				.append("resultText", 1)
				.append("appointDate", 1)
				.append("appointAmount", 1)
				.append("tel", 1)
				.append("nextTimeDate", 1)
				.append("createdDateTime", 1)
				.append("link_actionCode.actCode", 1)
				.append("link_resultCode.rstCode", 1);
			}
			fields.append("contractNo", 1);
			fields.append("isHold", 1);
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
			fields.append("taskDetail._id", 1);
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
			if(req.getIsHold() != null) {
				criteria.and("isHold").is(req.getIsHold());
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
			
			if(!isNotice) {
				LOG.debug("Get users");
				users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
				resp.setUsers(users);
			}
			
			if(aggCountResult == null) {
				LOG.info("Not found data");
				resp.setTotalItems(Long.valueOf(0));
				return resp;
			}
			
			BasicDBObject sort;
			
			if(StringUtils.isBlank(req.getColumnName())) {
				sort = new BasicDBObject("$sort", new BasicDBObject("createdDateTime", -1));
			} else {
				if(req.getColumnName().equals("taskDetail." + SYS_OWNER.getName())) {
					req.setColumnName("taskDetail." + SYS_OWNER_ID.getName());
				}
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
			
			if(isNotice) {
				LOG.debug("return for notice");
				resp.setTraceDatas(result);
				return resp;
			}
			
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
	
	public NoticeDownloadCriteriaResp exportNotices(JasperService jasperService, String productId, List<Map> traceDatas) throws Exception {
		try {	
			LOG.debug("Call groupByTemplate");
			Map<String, List> templates = groupByTemplate(traceDatas);
			
			LOG.debug("Call genPdf");
			List<String> pdfFiles = genPdf(jasperService, productId, templates);
			
			String mergedFileTmp = FilenameUtils.removeExtension(pdfFiles.get(0)) + "_merged.pdf";
			
			LOG.debug("Call mergePdf");
			mergePdf(pdfFiles, mergedFileTmp);
			
			FileInputStream mergeFile = new FileInputStream(mergedFileTmp);
			byte[] data = IOUtils.toByteArray(mergeFile);
			
			LOG.debug("Delete merged pdf file");
			mergeFile.close();
			FileUtils.deleteQuietly(new File(mergedFileTmp));
			
			NoticeDownloadCriteriaResp resp = new NoticeDownloadCriteriaResp();
			resp.setFillTemplate(true);
			resp.setData(data);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateHold(UpdateTraceResultCriteriaReq req) {		
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Criteria criteria;
			
			for (IsHoldModel isActive : req.getIsHolds()) {
				criteria = Criteria.where("id").is(isActive.getId());
				template.updateFirst(Query.query(criteria), Update.update("isHold", isActive.getIsHold()), TraceWork.class);						
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private Map<String, List> groupByTemplate(List<Map> traceDatas) {
		try {
			Map<String, List> templates = new HashMap<>();
			List<Map> taskDetails;
			Object templateIdObj;
			List<Map> dataLst;
			
			for (Map map : traceDatas) {
				if((taskDetails = (List)map.get("taskDetail")) == null || taskDetails.size() == 0) continue;
	
				if((templateIdObj = map.get("templateId")) == null) continue;
				
				if(templates.containsKey(templateIdObj.toString())) {
					templates.get(templateIdObj.toString()).add(map);
				} else {					
					dataLst = new ArrayList<>();
					dataLst.add(map);
					templates.put(templateIdObj.toString(), dataLst);
				}
			}
			return templates;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private List<String> genPdf(JasperService jasperService, String productId, Map<String, List> templates) throws Exception {
		try {
			NoticeFindCriteriaReq noticeReq = new NoticeFindCriteriaReq();
			noticeReq.setProductId(productId);
//			List<Map<String, String>> addresses = new ArrayList<>();
			List<String> pdfFiles = new ArrayList<>();
			Map<String, String> fileDetail;
			String pdfFile, filePath;
			List<String> idsAndAddr;
			List<Map> taskDetails;
			List<Map> value;
			String key;
						
			for(Map.Entry<String, List> entry : templates.entrySet()) {
			    key = entry.getKey();
			    value = entry.getValue();
			    noticeReq.setId(key);
				
				LOG.debug("Get file");
				fileDetail = noticeUploadService.getNoticeFile(noticeReq);
				
				if(fileDetail == null) {
					LOG.warn("Not found Notice file on");
					continue;
				}
					
				filePath = fileDetail.get("filePath");
				idsAndAddr = new ArrayList<>();
				
				for (Map m : value) {
					taskDetails = (List)m.get("taskDetail");
					idsAndAddr.add(String.valueOf(taskDetails.get(0).get("_id")) + "," + String.valueOf(m.get("addressNoticeStr")));
				}
												
				LOG.debug("Call exportNotices");
				pdfFile = jasperService.exportNotices(productId, filePath, idsAndAddr);
				pdfFiles.add(pdfFile);
			}
			
			return pdfFiles;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void mergePdf(List<String> pdfFiles, String mergedFile) throws Exception {
		Document document = new Document();
		
		try {
			LOG.debug("Amount of pdf file: " + pdfFiles.size());
			PdfCopy copy = new PdfCopy(document, new FileOutputStream(mergedFile));
			document.open();
			PdfReader reader;
			int n;
			
			for (String pdfFile : pdfFiles) {
				reader = new PdfReader(pdfFile);
				n = reader.getNumberOfPages();
				
				for (int page = 0; page < n; ) {
	                copy.addPage(copy.getImportedPage(reader, ++page));
	            }
				
	            copy.freeReader(reader);
	            reader.close();
	            FileUtils.deleteQuietly(new File(pdfFile));
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			document.close();
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