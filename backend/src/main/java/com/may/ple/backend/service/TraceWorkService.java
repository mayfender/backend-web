package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_APPOINT_AMOUNT;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_APPOINT_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_IS_ACTIVE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_NEXT_TIME_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_PROBATION_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_RESULT_TEXT;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_TEL;
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
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.bussiness.KrungsriApi;
import com.may.ple.backend.constant.ActionConstant;
import com.may.ple.backend.constant.SysFieldConstant;
import com.may.ple.backend.criteria.DymListFindCriteriaReq;
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
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.DymList;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.TraceResultReportFile;
import com.may.ple.backend.entity.TraceWork;
import com.may.ple.backend.entity.TraceWorkComment;
import com.may.ple.backend.entity.TraceWorkUpdatedHistory;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.IsHoldModel;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class TraceWorkService {
	private static final Logger LOG = Logger.getLogger(TraceWorkService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	private UserAction userAct;
	private NoticeUploadService noticeUploadService;
	private DymListService dymService;
	private UserService userService;
	private NotificationService noticService;
	private DymSearchService dymSearchService;

	@Autowired
	public TraceWorkService(MongoTemplate template, DbFactory dbFactory, UserAction userAct,
			NoticeUploadService noticeUploadService, DymListService dymService, UserService userService,
			NotificationService noticService, DymSearchService dymSearchService) {
		this.templateCore = template;
		this.dbFactory = dbFactory;
		this.userAct = userAct;
		this.noticeUploadService = noticeUploadService;
		this.dymService = dymService;
		this.userService = userService;
		this.noticService = noticService;
		this.dymSearchService = dymSearchService;
	}

	public TraceFindCriteriaResp find(TraceFindCriteriaReq req) throws Exception {
		try {
			TraceFindCriteriaResp resp = new TraceFindCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			//------------------------------------------------------------------------------
			List<Integer> statuses = new ArrayList<>();
			statuses.add(0);
			statuses.add(1);

			DymListFindCriteriaReq reqDym = new DymListFindCriteriaReq();
			reqDym.setStatuses(statuses);
			reqDym.setProductId(req.getProductId());
			List<DymList> dymList = dymService.findList(reqDym);
			String tableName;
			Criteria criteria = Criteria.where("contractNo").is(req.getContractNo());
			if(req.getIsOldTrace() != null && req.getIsOldTrace()) {
				tableName = "traceWorkOld";
			} else {
				tableName = "traceWork";
			}

			//------------------------------------------------------------------------------
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("createdDateTime", -1));

			BasicDBObject fields = new BasicDBObject()
			.append("resultText", 1)
			.append("tel", 1)
			.append("appointDate", 1)
			.append("nextTimeDate", 1)
			.append("contractNo", 1)
			.append("createdDateTime", 1)
			.append("appointAmount", 1)
			.append("createdByName", 1)
			.append("templateId", 1)
			.append("addressNotice", 1)
			.append("isReadOnly", 1)
			.append("uploadStatusCode", 1)
			.append("uploadStatusDesc", 1)
			.append("taskDetail.sys_owner", 1);

			MatchOperation match = Aggregation.match(criteria);

			LOG.debug("Start count");
			Aggregation agg = Aggregation.newAggregation(
					match,
					Aggregation.group().count().as("totalItems")
			);

			AggregationResults<Map> aggregate = template.aggregate(agg, tableName, Map.class);
			Map aggCountResult = aggregate.getUniqueMappedResult();
			LOG.debug("End count");

			if(aggCountResult == null) {
				LOG.info("Not found data");
				resp.setTotalItems(Long.valueOf(0));
				return resp;
			}

			//---------------------------------------
			List<AggregationOperation> aggregateLst = new ArrayList<>();
			aggregateLst.add(match);
			aggregateLst.add(new CustomAggregationOperation(sort));
			aggregateLst.add(Aggregation.skip((req.getCurrentPage() - 1) * req.getItemsPerPage()));
			aggregateLst.add(Aggregation.limit(req.getItemsPerPage()));

			DymList dymlst;
			for (int i = 0; i < dymList.size(); i++) {
				dymlst = dymList.get(i);

				if(dymlst.getFieldName() == null) continue;

				if(dymlst.getType() == null || dymlst.getType() == 1) {
					fields.append("link_" + dymlst.getFieldName(), 1);
				} else {
					fields.append(dymlst.getFieldName(), 1);
				}

				aggregateLst.add(new CustomAggregationOperation(
								        new BasicDBObject(
									            "$lookup",
									            new BasicDBObject("from", "dymListDet")
									                .append("localField", dymlst.getFieldName())
									                .append("foreignField", "_id")
									                .append("as", "link_" + dymlst.getFieldName())
									        )));
			}

			BasicDBObject project = new BasicDBObject("$project", fields);
			aggregateLst.add(new CustomAggregationOperation(project));

			agg = Aggregation.newAggregation(aggregateLst.toArray(new AggregationOperation[aggregateLst.size()]));
			aggregate = template.aggregate(agg, tableName, Map.class);
			List<Map> result = aggregate.getMappedResults();

			resp.setTotalItems(((Integer)aggCountResult.get("totalItems")).longValue());
			resp.setTraceWorks(result);

			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Map<String, Object> save(TraceSaveCriteriaReq req, Users userCreated, String fileId) throws Exception {
		try {
			LOG.debug("Get user");
			Date date = Calendar.getInstance().getTime();

			Calendar calNoTime = Calendar.getInstance();
			calNoTime.set(Calendar.HOUR_OF_DAY, 0);
			calNoTime.set(Calendar.MINUTE, 0);
			calNoTime.set(Calendar.SECOND, 0);
			calNoTime.set(Calendar.MILLISECOND, 0);
			Date dateNotime = calNoTime.getTime();

			if(userCreated == null) {
				userCreated = ContextDetailUtil.getCurrentUser(templateCore);
			}
			Boolean probation = userCreated.getProbation();
			Map<String, Object> traceWork;
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Date dummyDate = new Date(Long.MAX_VALUE);
			List<Map> dymListVal = req.getDymListVal();
			Map<String, String> userOwner = null;
			Calendar cal;
			Object value;
			Integer type;

			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting prdSetting = product.getProductSetting();
			boolean isAPIUpload = false;
			Update updateOnsave = null;

			//---: Remove all Unicode linebreak;
			req.setResultText(req.getResultText().replaceAll("\\R+", " "));

			if(StringUtils.isBlank(req.getId())) {

				//---:
				Map krungSriAPISetting = prdSetting.getKrungSriAPI();
				List<String> apiFields = null;
				if(krungSriAPISetting != null && krungSriAPISetting.get("enable") != null && (int)krungSriAPISetting.get("enable") == 1) {
					isAPIUpload = true;
					KrungsriApi krsApi = KrungsriApi.getInstance();
					apiFields = krsApi.getTaskDetailFields(krungSriAPISetting.get("dataFormat").toString());
				}

				traceWork = new HashMap<>();
				traceWork.put("createdDateTime", date);
				traceWork.put("contractNo", req.getContractNo());
				traceWork.put("idCardNo", req.getIdCardNo());
				traceWork.put("createdBy", userCreated.getId());
				traceWork.put("createdByName", userCreated.getShowname());
				traceWork.put("isHold", false);

				if(StringUtils.isNoneBlank(fileId)) {
					traceWork.put("fileId", fileId);
					traceWork.put("isImported", true);
				}

				updateOnsave = new Update();
				updateOnsave.set(SYS_TRACE_DATE.getName(), date);
				updateOnsave.set(SYS_RESULT_TEXT.getName(), req.getResultText());
				updateOnsave.set(SYS_TEL.getName(), req.getTel());

				if(prdSetting.getUpdateEmptyReminderDate() != null && prdSetting.getUpdateEmptyReminderDate().intValue() == 1) {
					updateOnsave.set(SYS_APPOINT_DATE.getName(), req.getAppointDate() == null ? dummyDate : req.getAppointDate());
					updateOnsave.set(SYS_NEXT_TIME_DATE.getName(), req.getNextTimeDate() == null ? dummyDate : req.getNextTimeDate());
					updateOnsave.set(SYS_APPOINT_AMOUNT.getName(), req.getAppointAmount());
				} else {
					if(req.getAppointDate() != null) {
						updateOnsave.set(SYS_APPOINT_DATE.getName(), req.getAppointDate());
					}
					if(req.getNextTimeDate() != null) {
						updateOnsave.set(SYS_NEXT_TIME_DATE.getName(), req.getNextTimeDate());
					}
					if(req.getAppointAmount() != null) {
						updateOnsave.set(SYS_APPOINT_AMOUNT.getName(), req.getAppointAmount());
					}
				}

				//--: Save taskDetail data as well.
				LOG.debug("Save others taskDetail data as well");
				List<ColumnFormat> headers = product.getColumnFormats();
				headers = getColumnFormatsActive(headers);
				ColumnFormat columnFormatProbation = new ColumnFormat();
				columnFormatProbation.setColumnName(SYS_PROBATION_OWNER_ID.getName());
				headers.add(columnFormatProbation);

				String contractNoColumn = prdSetting.getContractNoColumnName();
				Query query = Query.query(Criteria.where(contractNoColumn).is(req.getContractNo()));
				Field fields = query.fields().include(SYS_OWNER_ID.getName());

				boolean isExis = template.collectionExists(TraceWork.class);

				//--: Manage index
				DBCollection collection = null;
				if(isExis) {
					collection = template.getCollection("traceWork");
					collection.createIndex(new BasicDBObject("createdDateTime", 1));
					collection.createIndex(new BasicDBObject("contractNo", 1));
					collection.createIndex(new BasicDBObject("nextTimeDate", 1));
					collection.createIndex(new BasicDBObject("appointDate", 1));
					collection.createIndex(new BasicDBObject("appointAmount", 1));
					collection.createIndex(new BasicDBObject("tel", 1));
				}

				for (ColumnFormat colForm : headers) {
					fields.include(colForm.getColumnName());

					//---: API
					if(apiFields != null) {
						if(apiFields.contains(colForm.getColumnName())) {
							apiFields.remove(colForm.getColumnName());
						}
					}

					if(isExis) {
						collection.createIndex(new BasicDBObject("taskDetail." + colForm.getColumnName(), 1));
					}
				}

				//---: API
				if(apiFields != null) {
					for (String apiFd : apiFields) {
						fields.include(apiFd);
					}
					if(isExis) {
						collection.createIndex(new BasicDBObject("uploadStatusCode", 1));
					}
				}

				for (Map m : dymListVal) {
					value = m.get("value");
					type = (Integer)m.get("type");

					if(type != null && type.intValue() > 1) {
						if(value != null) {
							if(type.intValue() == 2) {
								value = value.toString();
							} else if(type.intValue() == 3) {
								value = Double.valueOf(value.toString());
							} else if(type.intValue() == 4) {
								cal = Calendar.getInstance();
								cal.setTimeInMillis(Long.valueOf(value.toString()));
								value = cal.getTime();
							}
						}

						updateOnsave.set(m.get("fieldName").toString(), value);
						continue;
					}

					//--------------------
					if(isExis) {
						collection.createIndex(new BasicDBObject(m.get("fieldName").toString(), 1));
					}

					if(m.containsKey("isSuspend") && m.get("isSuspend") != null && (Boolean)m.get("isSuspend")) {
						updateOnsave.set("sys_suspendedDateTime", dateNotime);
						traceWork.put("sys_suspendedDateTime", dateNotime);
						template.getCollection(NEW_TASK_DETAIL.getName()).createIndex(new BasicDBObject("sys_suspendedDateTime", 1));
					}

					updateOnsave.set(m.get("fieldName").toString(), value == null ? null : new ObjectId(value.toString()));
				}

				//--: Update TaskDetail
//				LOG.debug("Update taskdetail appoint-date and next-time-date");
//				template.updateFirst(Query.query(Criteria.where("_id").is(req.getTaskDetailId())), update, NEW_TASK_DETAIL.getName());

				LOG.debug("Find taskDetail");
				Map taskDetail = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());

				LOG.debug("Find users");
				List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
				List<String> ownerId = (List)taskDetail.get(SYS_OWNER_ID.getName());
				List<Map<String, String>> userList = MappingUtil.matchUserId(users, ownerId.get(0));
				userOwner = (Map)userList.get(0);
				taskDetail.put(SYS_OWNER.getName(), userOwner.get("showname"));

				if(probation != null && probation) {
					traceWork.put("createdBy", ownerId.get(0));
					traceWork.put("createdByName", userOwner.get("showname"));
				}

				traceWork.put("taskDetail", taskDetail);

				//--: Response
				req.setTraceDate(date);

				if(!prdSetting.getIsHideAlert()) {
					//--: Add new notification
					noticService.traceBooking(req.getAppointDate(), req.getNextTimeDate(),
							req.getContractNo(), req.getProductId(), req.getResultText(), userOwner.get("id"));
				}
			} else {
				traceWork = template.findOne(Query.query(Criteria.where("_id").is(req.getId())), Map.class, "traceWork");

				//---: Save updated trace data history
				LOG.info("Save updated data as history");
				Map<String, Object> traceHis = new HashedMap(traceWork);
//				BeanUtils.copyProperties(traceHis, traceWork);

				traceHis.put("_id", null);
				traceHis.put("createdDateTime", date);
				traceHis.put("traceWorkId", new ObjectId(traceWork.get("_id").toString()));
				traceHis.put("action", ActionConstant.UPDATED.getName());

				template.save(traceHis, "traceWorkUpdatedHistory");

				DBCollection collection = template.getCollection("traceWorkUpdatedHistory");
				collection.createIndex(new BasicDBObject("createdDateTime", 1));
				collection.createIndex(new BasicDBObject("traceWorkId", 1));

				//--: update
				traceWork.put("updatedBy", userCreated.getId());

				Query q = Query.query(Criteria.where("contractNo").is(traceWork.get("contractNo")));
				q.with(new Sort(Sort.Direction.DESC, "createdDateTime"));
				q.fields().include("_id").include("taskDetail." + SYS_OWNER_ID.getName());

				Map lastestTrace = template.findOne(q, Map.class, "traceWork");

				if(lastestTrace.get("_id").toString().equals(req.getId())) {
					LOG.info("Update " + SYS_APPOINT_DATE.getName() + " and " + SYS_NEXT_TIME_DATE.getName() + " also.");

					Update update = new Update();
					update.set(SYS_RESULT_TEXT.getName(), req.getResultText());
					update.set(SYS_TEL.getName(), req.getTel());
					update.set(SYS_APPOINT_DATE.getName(), req.getAppointDate() == null ? dummyDate : req.getAppointDate());
					update.set(SYS_NEXT_TIME_DATE.getName(), req.getNextTimeDate() == null ? dummyDate : req.getNextTimeDate());
					update.set(SYS_APPOINT_AMOUNT.getName(), req.getAppointAmount());

					boolean isSuspendedExit = false, isSuspended = false;
					if(traceWork.containsKey("sys_suspendedDateTime") && traceWork.get("sys_suspendedDateTime") != null) {
						isSuspendedExit = true;
					}

					for (Map m : dymListVal) {
						value = m.get("value");
						type = (Integer)m.get("type");

						if(type != null && type.intValue() > 1) {
							if(value != null) {
								if(type.intValue() == 2) {
									value = value.toString();
								} else if(type.intValue() == 3) {
									value = Double.valueOf(value.toString());
								} else if(type.intValue() == 4) {
									cal = Calendar.getInstance();
									cal.setTimeInMillis(Long.valueOf(value.toString()));
									value = cal.getTime();
								}
							}

							update.set(m.get("fieldName").toString(), value);
							continue;
						}

						//----------------------
						if(m.containsKey("isSuspend") && m.get("isSuspend") != null && (Boolean)m.get("isSuspend")) {
							isSuspended = true;
						}

						if(type == null || type.intValue() == 1) {
							update.set(m.get("fieldName").toString(), value == null ? null : new ObjectId(value.toString()));
						}
					}

					if(isSuspendedExit) {
						if(!isSuspended) {
							update.set("sys_suspendedDateTime", null);
							traceWork.put("sys_suspendedDateTime", null);
						}
					} else {
						if(isSuspended) {
							update.set("sys_suspendedDateTime", dateNotime);
							traceWork.put("sys_suspendedDateTime", dateNotime);
						}
					}

					template.updateFirst(Query.query(Criteria.where("_id").is(req.getTaskDetailId())), update, NEW_TASK_DETAIL.getName());

					if(!prdSetting.getIsHideAlert()) {
						//--: Add new notification or update
						String userId = ((List)((Map)lastestTrace.get("taskDetail")).get(SYS_OWNER_ID.getName())).get(0).toString();
						noticService.traceBooking(req.getAppointDate(), req.getNextTimeDate(),
								req.getContractNo(), req.getProductId(), req.getResultText(), userId);
					}
				}
			}

			traceWork.put("resultText", req.getResultText());
			traceWork.put("tel", req.getTel());
			traceWork.put("appointDate", req.getAppointDate());
			traceWork.put("nextTimeDate", req.getNextTimeDate());
			traceWork.put("appointAmount", req.getAppointAmount());
			traceWork.put("templateId", req.getTemplateId() == null ? null: new ObjectId(req.getTemplateId()));
			traceWork.put("addressNotice", req.getAddressNotice());
			traceWork.put("addressNoticeStr", req.getAddressNoticeStr());
			traceWork.put("updatedDateTime", date);

			for (Map m : dymListVal) {
				value = m.get("value");
				type = (Integer)m.get("type");

				if(value != null) {
					if(type == null || type.intValue() == 1) {
						value = new ObjectId(value.toString());
					} else if(type.intValue() == 2) {
						value = value.toString();
					} else if(type.intValue() == 3) {
						value = Double.valueOf(value.toString());
					} else if(type.intValue() == 4) {
						cal = Calendar.getInstance();
						cal.setTimeInMillis(Long.valueOf(value.toString()));
						value = cal.getTime();
					}
				}

				traceWork.put(m.get("fieldName").toString(), value);
			}

			//---: Check to push to cloud.
			String uploadStatusCode = null;
			String uploadStatusDesc = "Success";
			Map krungSriAPISetting = prdSetting.getKrungSriAPI();
			boolean isCallApi = false;

			if(isAPIUpload) {
				try {
					LOG.info("Start call KrunkSri API.");
					KrungsriApi krsApi = KrungsriApi.getInstance();
					krsApi.initParams(krungSriAPISetting);

					Map<String, String> uploadDataMap = krsApi.prepareData(
							traceWork,
							krungSriAPISetting.get("dataFormat").toString(),
							req.getProductId(),
							userOwner,
							userCreated,
							dymService,
							fileId
							);

					if(uploadDataMap.containsKey("isUat")) {
						if(uploadDataMap.get("isUat").equals("1")) {
							LOG.info("UAT Test case");
							isCallApi = true;
						}
					} else {
						isCallApi = true;
					}

					if(isCallApi) {
						JsonObject responseJson = krsApi.uploadJson(uploadDataMap);
						String[] responseCode = getResponseCode(responseJson);
						uploadStatusCode = responseCode[0];
						uploadStatusDesc = responseCode[1];
						LOG.info(responseJson.toString());
					}

					LOG.info("End call KrunkSri API.");
				} catch (Exception e) {
					uploadStatusCode = "500";
					uploadStatusDesc = e.toString();
					LOG.error(e.toString(), e);
				}
			}

			Map<String, Object> response = new HashMap<>();

			//---: API
			if(uploadStatusCode != null) {
				traceWork.put("uploadStatusCode", uploadStatusCode);
				traceWork.put("uploadStatusDesc", uploadStatusDesc);
				response.put("uploadStatusCode", uploadStatusCode);

				Map<String, Object> retryError = KrungsriApi.getInstance().getRetryError(uploadStatusCode);
				if(retryError.size() > 0) {
					response.put("retryMsg", retryError.get("errMsg"));
				}

				if(uploadStatusCode.equals("0000I") || retryError.size() == 0 || StringUtils.isNoneBlank(fileId)) {
					LOG.info("Save");
					template.save(traceWork, "traceWork");

					//--: Update TaskDetail
					LOG.debug("Update taskdetail appoint-date and next-time-date");
					template.updateFirst(Query.query(Criteria.where("_id").is(req.getTaskDetailId())), updateOnsave, NEW_TASK_DETAIL.getName());
				} else {
					LOG.info("Not save and inform user error to retry again.");
				}
			} else {
				LOG.info("Save");
				template.save(traceWork, "traceWork");

				if(updateOnsave != null) {
					//--: Update TaskDetail
					LOG.debug("Update taskdetail appoint-date and next-time-date");
					template.updateFirst(Query.query(Criteria.where("_id").is(req.getTaskDetailId())), updateOnsave, NEW_TASK_DETAIL.getName());
				}
			}

			return response;
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

			query = Query.query(Criteria.where("contractNo").is(contractNo));
			query.with(new Sort(Sort.Direction.DESC, "createdDateTime"));
			query.limit(1);

			Map tracWork = template.findOne(query, Map.class, "traceWork");
			Date dummyDate = new Date(Long.MAX_VALUE);
			Update update = new Update();

			LOG.debug("dymList");
			List<Integer> statuses = new ArrayList<>();
			statuses.add(1);
			DymListFindCriteriaReq reqDym = new DymListFindCriteriaReq();
			reqDym.setStatuses(statuses);
			reqDym.setProductId(productId);
			List<Map> dymList = dymService.findFullList(reqDym, false);

			if(tracWork == null) {
				update.set(SYS_APPOINT_DATE.getName(), dummyDate);
				update.set(SYS_APPOINT_AMOUNT.getName(), null);
				update.set(SYS_NEXT_TIME_DATE.getName(), dummyDate);
				update.set(SYS_TRACE_DATE.getName(), dummyDate);
				update.set("sys_suspendedDateTime", null);

				for (Map map : dymList) {
					update.set(map.get("fieldName").toString(), null);
				}
			} else {
				update.set(SYS_APPOINT_DATE.getName(), tracWork.containsKey("appointDate") ? tracWork.get("appointDate") : dummyDate);
				update.set(SYS_APPOINT_AMOUNT.getName(), tracWork.containsKey("appointAmount") ?  tracWork.get("appointAmount") : null);
				update.set(SYS_NEXT_TIME_DATE.getName(), tracWork.containsKey("nextTimeDate") ?  tracWork.get("nextTimeDate") : dummyDate);
				update.set(SYS_TRACE_DATE.getName(), tracWork.containsKey("createdDateTime") ?  tracWork.get("createdDateTime") : dummyDate);
				update.set("sys_suspendedDateTime", tracWork.containsKey("sys_suspendedDateTime") ?  tracWork.get("sys_suspendedDateTime") : dummyDate);

				for (Map map : dymList) {
					update.set(map.get("fieldName").toString(), tracWork.containsKey(map.get("fieldName").toString()) ? tracWork.get(map.get("fieldName").toString()) : null);
				}
			}
			template.updateFirst(Query.query(Criteria.where("_id").is(taskDetailId)), update, NEW_TASK_DETAIL.getName());

			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List<Map> noTraceResult(List<Map> traceDatas, BasicDBObject fields, String productId) throws Exception {
		try {
			LOG.debug("Find users");
			List<Users> users = userAct.getUserByProductToAssign(productId).getUsers();
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			List<String> uIds = new ArrayList<>();

			for (Users u : users) uIds.add(u.getId());

			LOG.debug("Users size: " + uIds.size());

			Criteria criteria = Criteria.where(SYS_OWNER_ID.getName() + ".0").in(uIds)
								.and(SYS_IS_ACTIVE.getName() + ".status").is(true);
			Query query = Query.query(criteria);
			Field fObj = query.fields();
			fObj.include(SYS_OWNER_ID.getName());

			Set<String> keySet = fields.keySet();

			for (String key : keySet) {
				if(key.startsWith("taskDetailFull")) {
					fObj.include(key.replace("taskDetailFull.", ""));
				}
			}

			LOG.debug("Start find taskDetail");
			List<Map> taskDetails = template.find(query, Map.class, NEW_TASK_DETAIL.getName());
			LOG.debug("End find taskDetail");

			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			String contactColumn = product.getProductSetting().getContractNoColumnName();
			List<Map> noTraceTask = new ArrayList<>();
			List<Map> traceDatasDummy = null;
			String contactCol;

			if(traceDatas != null) {
				traceDatasDummy = new ArrayList<>(traceDatas);
			}

			Date date = Calendar.getInstance().getTime();
			List<Map<String, String>> userList;
			List<Map> taskDeatils;
			Map traceWorkMock;
			List<String> userIds;

			outer: for (Map task : taskDetails) {
				if(traceDatas != null) {
					contactCol = task.get(contactColumn).toString();

					for (Map map : traceDatasDummy) {
						if(map.get("contractNo").toString().equals(contactCol)) {
							traceDatasDummy.remove(map);
							continue outer;
						}
					}
				}

				//--: Make data for no trace.
				traceWorkMock = new HashMap();
				traceWorkMock.put("createdDateTime", date);
				userIds = (List)task.get(SYS_OWNER_ID.getName());
				Map user;

				if(userIds != null) {
					userList = MappingUtil.matchUserId(users, userIds.get(0));
					if(userList != null && userList.size() > 0) {
						user = new HashMap<>();
						user.put("sys_owner", userList.get(0).get("showname"));
						traceWorkMock.put("taskDetail", user);
						traceWorkMock.put("createdBy", userList.get(0).get("id")); // Just set to not empty.
					}
				}

				taskDeatils = new ArrayList<>();
				taskDeatils.add(task);

				traceWorkMock.put("taskDetailFull", taskDeatils);

				noTraceTask.add(traceWorkMock);
			}

			return noTraceTask;
		} catch (Exception e) {
			throw e;
		}
	}

	public TraceResultCriteriaResp traceResult(TraceResultCriteriaReq req, BasicDBObject fields, boolean isNotice) throws Exception {
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
			resp.setCreatedByLog(productSetting.getCreatedByLog());

			//---: API Flag
			Map krungSriAPISetting = productSetting.getKrungSriAPI();
			resp.setOnApi(krungSriAPISetting == null ? null : (int)krungSriAPISetting.get("enable"));

			String contactColumn = productSetting.getContractNoColumnName();
			List<ColumnFormat> headers = product.getColumnFormats();
			if(headers == null) return resp;

			headers = getColumnFormatsActive(headers);
			resp.setHeaders(headers);
			List<Criteria> multiOrTaskDetail = new ArrayList<>();
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			List<String> probationUserIds = new ArrayList<>();
			BasicDBObject sort;
			boolean isPreparedFields = fields != null ? true : false;

			for (Users u : users) {
				if(u.getProbation() == null || !u.getProbation()) continue;
				probationUserIds.add(u.getId());
			}

			if(fields == null) {
				fields = new BasicDBObject()
				.append("resultText", 1)
				.append("appointDate", 1)
				.append("appointAmount", 1)
				.append("tel", 1)
				.append("nextTimeDate", 1)
				.append("createdDateTime", 1)
				.append("createdByName", 1)
				.append("taskDetail." + SYS_OWNER.getName(), 1);
			}
			fields.append("createdBy", 1);
			fields.append("contractNo", 1);
			fields.append("isHold", 1);
			fields.append("uploadStatusCode", 1);
			fields.append("uploadStatusDesc", 1);
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
				multiOrTaskDetail.add(Criteria.where("resultText").regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));
				multiOrTaskDetail.add(Criteria.where("tel").regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));
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
				criteria.and(req.getCodeName()).is(new ObjectId(req.getCodeValue()));
			}

			if(!StringUtils.isBlank(req.getDymSearchFiedVal())) {
				criteria.and("taskDetail." + req.getDymSearchFiedName()).is(req.getDymSearchFiedVal());
			}

			if(req.getIsHold() != null) {
				criteria.and("isHold").is(req.getIsHold());
			}
			if(req.getApiUploadStatus() != null) {
				if(req.getApiUploadStatus() == 1) {
					criteria.and("uploadStatusCode").is("0000I");
				} else if(req.getApiUploadStatus() == 2) {
					List<String> ninCodes = new ArrayList<>();
					ninCodes.add("0000I");
					ninCodes.add("DMS_IGN");
					criteria.and("uploadStatusCode").nin(ninCodes);
				} else if(req.getApiUploadStatus() == 3) {
					criteria.and("uploadStatusCode").is("DMS_IGN");
				}
			}

			Criteria[] multiOrArr = multiOrTaskDetail.toArray(new Criteria[multiOrTaskDetail.size()]);
			if(multiOrArr.length > 0) {
				criteria.orOperator(multiOrArr);
			}

			//-----------------------------------------------------------
			List<Integer> statuses = new ArrayList<>();
			statuses.add(1);

			DymListFindCriteriaReq reqDym = new DymListFindCriteriaReq();
			reqDym.setStatuses(statuses);
			reqDym.setProductId(req.getProductId());

			List<Map> dymList = dymService.findFullList(reqDym, false);
			resp.setDymList(dymList);
			resp.setDymSearch(dymSearchService.getFields(req.getProductId(), statuses));

			//-----------------------------------------------------------
			if(req.getIsInit() != null && req.getIsInit()) {
				Query queryTemplate = Query.query(Criteria.where("enabled").is(true));
				queryTemplate.with(new Sort("templateName"));
				queryTemplate.fields().include("templateName");
				List<TraceResultReportFile> uploadTemplates = template.find(queryTemplate, TraceResultReportFile.class);
				resp.setUploadTemplates(uploadTemplates);
			}

			//------------------------------------------------------------
			AggregationResults<Map> aggregate = null;
			Map aggCountResult = null;
			Aggregation aggCount = null;

			if(!isNotice) {
				LOG.debug("Get users");
				resp.setUsers(users);

				LOG.debug("Start count");
				aggCount = Aggregation.newAggregation(
						Aggregation.match(criteria),
						Aggregation.group().count().as("totalItems")
				);

				aggregate = template.aggregate(aggCount, "traceWork", Map.class);
				aggCountResult = aggregate.getUniqueMappedResult();
				LOG.debug("End count");

				if(aggCountResult == null) {
					LOG.info("Not found data");
					resp.setTotalItems(Long.valueOf(0));
					return resp;
				} else {
					resp.setTotalItems(((Integer)aggCountResult.get("totalItems")).longValue());
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

			if(!isNotice) {
				aggregateLst.add(Aggregation.skip((req.getCurrentPage() - 1) * req.getItemsPerPage()));
				aggregateLst.add(Aggregation.limit(req.getItemsPerPage()));
			} else {
				if(req.getCurrentPage() != null) {
					aggregateLst.add(Aggregation.skip((req.getCurrentPage() - 1) * req.getItemsPerPage()));
					aggregateLst.add(Aggregation.limit(req.getItemsPerPage()));
				}

				aggregateLst.add(new CustomAggregationOperation(
				        new BasicDBObject(
				            "$lookup",
				            new BasicDBObject("from", NEW_TASK_DETAIL.getName())
				                .append("localField","contractNo")
				                .append("foreignField", contactColumn)
				                .append("as", "taskDetailFull")
				        )
					));
			}

			aggregateLst.add(new CustomAggregationOperation(
			        new BasicDBObject(
			            "$lookup",
			            new BasicDBObject("from", "address")
			                .append("localField","_id")
			                .append("foreignField", "traceId")
			                .append("as", "link_address")
			        )
				));

			Map dymLst;
			Integer type;
			for (int i = 0; i < dymList.size(); i++) {
				dymLst = dymList.get(i);

				if(dymLst.get("fieldName") == null) continue;

				if(!isPreparedFields) {
					type = (Integer)dymLst.get("type");
					if(type == null || type.intValue() == 1) {
						fields.append("link_" + dymLst.get("fieldName"), 1);
					} else {
						fields.append(dymLst.get("fieldName").toString(), 1);
					}
				}

				aggregateLst.add(new CustomAggregationOperation(
								        new BasicDBObject(
									            "$lookup",
									            new BasicDBObject("from", "dymListDet")
									                .append("localField", dymLst.get("fieldName"))
									                .append("foreignField", "_id")
									                .append("as", "link_" + dymLst.get("fieldName"))
									        )));
			}

			aggregateLst.add(new CustomAggregationOperation(project));

			aggCount = Aggregation.newAggregation(aggregateLst.toArray(new AggregationOperation[aggregateLst.size()]));
			aggregate = template.aggregate(aggCount, "traceWork", Map.class);
			List<Map> result = aggregate.getMappedResults();

			if(isNotice) {
				LOG.debug("return for notice");
				resp.setTraceDatas(result);
				return resp;
			}

			List<Map> address;
			String addrFormatStr = "";

			for (Map map : result) {
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

//			Criteria criteria = Criteria.where("contractNo").is(req.getContractNo()).and("parentId").is(null);
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
			}/* else {
				template.remove(Query.query(Criteria.where("parentId").is(new ObjectId(comment.getId()))), TraceWorkComment.class);
			}*/

			/*String commentStr = req.getComment();
			if(!StringUtils.isBlank(commentStr)) {
				String parentId = null;
				boolean isLast = false;
				int lengthSub = 348;
				String commentResult;
				int order = 0;

				while(true) {
					int length = commentStr.length();
					if(length == 0) break;

					if(length > lengthSub) {
						commentResult = commentStr.substring(0, lengthSub);
						commentStr = commentStr.substring(lengthSub);
					} else {
						commentResult = commentStr;
						isLast = true;
					}

					comment.setComment(commentResult);
					comment.setUpdatedDateTime(date);
					comment.setUpdatedBy(user.getId());
					comment.setOrder(order);
					template.save(comment);

					if(isLast) break;

					if(order == 0) {
						parentId = comment.getId();
					}

					comment = new TraceWorkComment();
					comment.setCreatedDateTime(date);
					comment.setCreatedBy(user.getId());
					comment.setContractNo(req.getContractNo());
					comment.setParentId(parentId == null ? null : new ObjectId(parentId));

					order++;
				}
			} else {
				comment.setComment(commentStr);
				comment.setUpdatedDateTime(date);
				comment.setUpdatedBy(user.getId());
				template.save(comment);
			}*/


			comment.setComment(req.getComment());
			comment.setUpdatedDateTime(date);
			comment.setUpdatedBy(user.getId());

			template.save(comment);

			LOG.debug("Check and create Index.");
			DBCollection collection = template.getCollection("traceWorkComment");
			collection.createIndex(new BasicDBObject("contractNo", 1));
//			collection.createIndex(new BasicDBObject("comment", 1));

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
//			query.with(new Sort("order"));

			LOG.debug("Find");
			TraceWorkComment comment = template.findOne(query, TraceWorkComment.class);
//			List<TraceWorkComment> comments = template.find(query, TraceWorkComment.class);
//			TraceWorkComment result = null;

			/*if(comments != null && comments.size() > 0) {
				String commentResult = "";

				for (TraceWorkComment comm : comments) {
					commentResult += comm.getComment();
				}

				result = comments.get(0);
				result.setComment(commentResult);
			}*/

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

	public List<Map> getHis(String prodId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(prodId);
			Criteria criteria = Criteria.where("traceWorkId").is(new ObjectId(id));

			MatchOperation match = Aggregation.match(criteria);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("createdDateTime", -1));

			List<AggregationOperation> aggregateLst = new ArrayList<>();
			aggregateLst.add(match);
			aggregateLst.add(new CustomAggregationOperation(sort));
			aggregateLst.add(Aggregation.limit(5));

			List<Integer> statuses = new ArrayList<>();
			statuses.add(1);

			DymListFindCriteriaReq reqDym = new DymListFindCriteriaReq();
			reqDym.setStatuses(statuses);
			reqDym.setProductId(prodId);
			List<DymList> dymList = dymService.findList(reqDym);
			BasicDBObject fields = new BasicDBObject();
			fields.append("resultText", 1);
			fields.append("tel", 1);
			fields.append("createdDateTime", 1);
			fields.append("appointDate", 1);
			fields.append("nextTimeDate", 1);
			fields.append("appointAmount", 1);
			DymList dymlst;

			for (int i = 0; i < dymList.size(); i++) {
				dymlst = dymList.get(i);
				fields.append("link_" + dymlst.getFieldName() + ".code", 1);
				fields.append("link_" + dymlst.getFieldName() + ".meaning", 1);

				aggregateLst.add(new CustomAggregationOperation(
								        new BasicDBObject(
									            "$lookup",
									            new BasicDBObject("from", "dymListDet")
									                .append("localField", dymlst.getFieldName())
									                .append("foreignField", "_id")
									                .append("as", "link_" + dymlst.getFieldName())
									        )));
			}

			BasicDBObject project = new BasicDBObject("$project", fields);
			aggregateLst.add(new CustomAggregationOperation(project));

			Aggregation agg = Aggregation.newAggregation(aggregateLst.toArray(new AggregationOperation[aggregateLst.size()]));
			AggregationResults<Map> aggregate = template.aggregate(agg, "traceWorkUpdatedHistory", Map.class);
			List<Map> result = aggregate.getMappedResults();

			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void updateTaskDetail(String productId, List<Map> traceDatas) {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			String contractNoColumn = product.getProductSetting().getContractNoColumnName();
			List<ColumnFormat> headers = product.getColumnFormats();
			headers = getColumnFormatsActive(headers);
			List<Map<String, String>> userList;
			List<String> ownerId;
			String contractNo;
			Map taskDetail;
			Update update;
			Field fields;
			Query query;
			Map userMap;

			LOG.debug("Find users");
			List<Users> users = userAct.getUserByProductToAssign(productId).getUsers();

			for (Map traceData : traceDatas) {
				if(!(traceData.get("taskDetail") == null)) continue;

				contractNo = traceData.get("contractNo").toString();

				query = Query.query(Criteria.where(contractNoColumn).is(contractNo));
				fields = query.fields().include(SYS_OWNER_ID.getName());

				for (ColumnFormat colForm : headers) {
					fields.include(colForm.getColumnName());
				}

				taskDetail = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());

				if(taskDetail == null) continue;

				ownerId = (List)taskDetail.get(SYS_OWNER_ID.getName());
				userList = MappingUtil.matchUserId(users, ownerId.get(0));

				if(userList == null || userList.size() < 1) continue;

				userMap = (Map)userList.get(0);
				taskDetail.put(SYS_OWNER.getName(), userMap.get("showname"));

				update = new Update();
				update.set("taskDetail", taskDetail);

				template.updateFirst(Query.query(Criteria.where("_id").is(traceData.get("_id"))), update, "traceWork");
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	/*public void traceUpload(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String productId) throws Exception {
		try (
				Workbook workbook = new XSSFWorkbook(uploadedInputStream)
			){

			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);
			Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet);

			List<Map<String, String>> data = new ArrayList<>();
			int lastRowNum = sheet.getLastRowNum();
			Map<String, String> itemMap;
			String val;
			Cell cell;
			Row row;

			for (int i = lastRowNum; i > 0; i--) {
				row = sheet.getRow(i);

				if(row == null) continue;

				itemMap = new HashMap<>();
				for (Entry<String, Integer> entrySet : headerIndex.entrySet()) {

					if(entrySet.getKey().endsWith("_ign")) continue;

					cell = row.getCell(entrySet.getValue(), MissingCellPolicy.RETURN_BLANK_AS_NULL);
					if(cell == null) continue;

					if(cell.getCellType() == Cell.CELL_TYPE_FORMULA){
						val = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell, formulaEvaluator));
					} else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC && HSSFDateUtil.isCellDateFormatted(cell)) {
						val = cell.getDateCellValue();
					} else {
						val = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell));
					}

					itemMap.put(entrySet.getKey(), val);
				}
				data.add(itemMap);
			}

			uploadProcess(productId, data);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}*/

	private void uploadProcess(String productId, List<Map<String, String>> data) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);

			String idField = "Tracking ID", uploadStatusField = "uploadStatus";
			Integer uploadStatus;
			String trackingId;
			Map traceWork;
			Query query;
			for (Map<String, String> item : data) {
				trackingId = item.get(idField).toString();

				query = Query.query(Criteria.where("_id").is(new ObjectId(trackingId)));
				query.fields().include(uploadStatusField);
				traceWork = template.findOne(query, Map.class, "traceWork");

				if(traceWork.get(uploadStatusField) != null) {
					uploadStatus = (Integer)traceWork.get(uploadStatusField);
				} else {
					uploadStatus = 0;
				}

				//---: To check that the items is uploaded or not.
				if(uploadStatus == 1) continue;

				//---: Prepare data to upload on cloud.
				item.remove(idField);

				//---: Call API
				JsonObject jsonObj = KrungsriApi.getInstance().uploadJson(item);
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
				if((taskDetails = (List)map.get("taskDetailFull")) == null || taskDetails.size() == 0) continue;

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
					taskDetails = (List)m.get("taskDetailFull");
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

	private String[] getResponseCode(JsonObject responseJson) {
		String err[] = new String[2];
		JsonElement errEl = responseJson.get("error");
		boolean isNull;

		if(errEl != null) {
			LOG.info("Error case 1");
			responseJson = errEl.getAsJsonObject();
			err[0] = responseJson.get("code").getAsString();
			err[1] = responseJson.get("message").getAsString();
		} else {
			JsonElement codeEl = responseJson.get("code");
			if(codeEl != null) {
				LOG.info("Error case 2");
				err[0] = responseJson.get("code").getAsString();
				isNull = responseJson.get("desc").isJsonNull();
				if(!isNull) {
					err[1] = responseJson.get("desc").getAsString();
				}
			} else {
				LOG.info("Error case 3");
				err[0] = responseJson.get("httpStatus").getAsString();
				err[1] = responseJson.get("httpStatusDesc").getAsString();
			}
		}
		return err;
	}

}