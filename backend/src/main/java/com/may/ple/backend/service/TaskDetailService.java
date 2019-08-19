package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_PAYMENT_DETAIL;
import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_APPOINT_AMOUNT;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_APPOINT_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_COMPARE_DATE_STATUS;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_IS_ACTIVE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_NEXT_TIME_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OLD_ORDER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_PROBATION_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_TAGS;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_TRACE_DATE;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.bussiness.ExcelReport;
import com.may.ple.backend.bussiness.ImportExcel;
import com.may.ple.backend.bussiness.TaskDetail;
import com.may.ple.backend.bussiness.UpdateByLoad;
import com.may.ple.backend.constant.AssignMethodConstant;
import com.may.ple.backend.constant.CompareDateStatusConstant;
import com.may.ple.backend.constant.TaskTypeConstant;
import com.may.ple.backend.criteria.AddressFindCriteriaReq;
import com.may.ple.backend.criteria.DymListFindCriteriaReq;
import com.may.ple.backend.criteria.TagsCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailCriteriaResp;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaResp;
import com.may.ple.backend.criteria.TaskUpdateDetailCriteriaReq;
import com.may.ple.backend.criteria.TraceCommentCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaResp;
import com.may.ple.backend.criteria.UpdateTaskIsActiveCriteriaReq;
import com.may.ple.backend.criteria.UpdateTaskIsActiveCriteriaResp;
import com.may.ple.backend.entity.Address;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.GroupData;
import com.may.ple.backend.entity.ImportMenu;
import com.may.ple.backend.entity.ImportOthersSetting;
import com.may.ple.backend.entity.IsActive;
import com.may.ple.backend.entity.NewTaskFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.TraceWorkComment;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.model.IsActiveModel;
import com.may.ple.backend.model.RelatedData;
import com.may.ple.backend.model.SumPayment;
import com.may.ple.backend.model.YearType;
import com.may.ple.backend.repository.UserRepository;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.may.ple.backend.utils.MergeColumnUtil;
import com.may.ple.backend.utils.RandomUtil;
import com.may.ple.backend.utils.TaskDetailStatusUtil;

@Service
public class TaskDetailService {
	private static final Logger LOG = Logger.getLogger(TaskDetailService.class.getName());
	private DbFactory dbFactory;
	private UserAction userAct;
	private UserService userService;
	private MongoTemplate templateCenter;
	private UserRepository userRepository;
	private TraceWorkService traceWorkService;
	private AddressService addressService;	
	private DymListService dymService;
	private DymSearchService dymSearchService;
	private ExcelReport excelUtil;
	
	@Autowired
	public TaskDetailService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct, UserRepository userRepository, 
			TraceWorkService traceWorkService, AddressService addressService, UserService userService, DymListService dymService,
			DymSearchService dymSearchService, ExcelReport excelUtil) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.userAct = userAct;
		this.userRepository = userRepository;
		this.traceWorkService = traceWorkService;
		this.addressService = addressService;
		this.userService = userService;
		this.dymService = dymService;
		this.dymSearchService = dymSearchService;
		this.excelUtil = excelUtil;
	}
	
	public TaskDetailCriteriaResp find(TaskDetailCriteriaReq req, List<String> fieldsParam) throws Exception {
		try {
			LOG.debug("Start find");
			TaskDetailCriteriaResp resp = new TaskDetailCriteriaResp();
			Date dummyDate = new Date(Long.MAX_VALUE);
			boolean isWorkingPage = false;
			boolean isRlatedData = false;
			boolean isAssign = false;
			
			if(!StringUtils.isBlank(req.getFromPage())) {				
				if(req.getFromPage().equals("working")) { 
					isWorkingPage = true;
				} else if(req.getFromPage().equals("related_data")) {
					isRlatedData = true;
				} else if(req.getFromPage().equals("assign") || req.getFromPage().equals("upload")) {
					isAssign = true;
				}
			}
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			List<ColumnFormat> columnFormatsPayment = product.getColumnFormatsPayment();
			ProductSetting productSetting = product.getProductSetting();
			
			LOG.debug("Call get USERS");
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			List<String> probationUserIds = new ArrayList<>();
			
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
			
			if(columnFormats == null) return resp;
			LOG.debug("Before size: " + columnFormats.size());
			columnFormats = getColumnFormatsActive(columnFormats, isAssign);
			LOG.debug("After size: " + columnFormats.size());
			
			columnFormatsPayment = getColumnFormatsActive(columnFormatsPayment, false);
			
			//-------------------------------------------------------------------------------------
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Criteria criteria;
			
			if(StringUtils.isBlank(req.getTaskFileId())) {
				Query queryFile = Query.query(Criteria.where("enabled").is(false));
				queryFile.fields().include("id");
				List<NewTaskFile> files = template.find(queryFile, NewTaskFile.class);
				List<String> fileIds = new ArrayList<>();
				for (NewTaskFile file : files) {
					fileIds.add(file.getId());
				}
				
				criteria = Criteria.where(SYS_FILE_ID.getName()).nin(fileIds);
			} else {				
				criteria = Criteria.where(SYS_FILE_ID.getName()).is(req.getTaskFileId());
			}
			
			if(StringUtils.isNotBlank(req.getContractNo())) {
				criteria.and(productSetting.getContractNoColumnName()).is(req.getContractNo());
			} else if(StringUtils.isNotBlank(req.getId())) {
				criteria.and("_id").is(new ObjectId(req.getId()));
			}
			
			if(req.getIsActive() != null) {
				criteria.and(SYS_IS_ACTIVE.getName() + ".status").is(req.getIsActive());
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
			
			if(!StringUtils.isBlank(req.getTag())) {
				criteria.and(SYS_TAGS.getName() + ".text").in(req.getTag());
			}
			
			if(!StringUtils.isBlank(req.getCodeValue())) {
				criteria.and(req.getCodeName()).is(new ObjectId(req.getCodeValue()));
			}
			
			if(!StringUtils.isBlank(req.getDymSearchFiedVal())) {
				criteria.and(req.getDymSearchFiedName()).is(req.getDymSearchFiedVal());
			}
			
			
			//------------------------------------------------------------------------------------------------------
			if(!StringUtils.isBlank(req.getOwner())) {
				if(req.getOwner().equals("-1")) {
					criteria.and(SYS_OWNER_ID.getName()).is(null);
				} else {
					Users user = userService.getUserById(req.getOwner());
					Boolean probation = user.getProbation();
					
					if(probation != null && probation) {						
						criteria.and(SYS_PROBATION_OWNER_ID.getName()).is(req.getOwner());										
					} else {
						if(probationUserIds.size() > 0) {
							criteria.and(SYS_PROBATION_OWNER_ID.getName()).nin(probationUserIds);
						}
						criteria.and(SYS_OWNER_ID.getName() + ".0").is(req.getOwner());										
					}					
				}					
			}
			//-------------------------------------------------------------------------------------------------------
			if(isWorkingPage) {
				criteria.and(SYS_OWNER_ID.getName()).ne(null);
			}
			if(isRlatedData) {
				criteria.and(productSetting.getIdCardNoColumnName()).is(req.getIdCardNo());
			}
			if(req.getIsNoTrace() != null && req.getIsNoTrace()) {
				criteria.and(SYS_TRACE_DATE.getName()).is(dummyDate);
			}
			if(req.getIsPgs() != null && req.getIsPgs()) {
				List<String> pgsIdNoLst = TaskDetail.getPgsIdNo(template);
				if(pgsIdNoLst == null) return resp;
				
				criteria.and(productSetting.getIdCardNoColumnName()).in(pgsIdNoLst);					
			}
			
			//-------------------------------------------------------------------------------------
			Query query = Query.query(criteria);
			Field fields = query.fields();
			
			//--: Include These fields alway because have to use its value.
			if(fieldsParam == null) {
				fields.include(SYS_OWNER_ID.getName());
				fields.include(SYS_APPOINT_DATE.getName());
				fields.include(SYS_APPOINT_AMOUNT.getName());
				fields.include(SYS_NEXT_TIME_DATE.getName());
				fields.include(SYS_TRACE_DATE.getName());
				
				if(isAssign) {
					fields.include(SYS_TAGS.getName());
				}
			}
			
			List<Criteria> multiOr = new ArrayList<>();
			Map<String, List<ColumnFormat>> sameColumnAlias = new HashMap<>();
			List<ColumnFormat> columRemovable = new ArrayList<>();
			String columnDummyAlias = "";
			List<ColumnFormat> columLst;
			
			for (ColumnFormat columnFormat : columnFormats) {
				//--: Concat fields
				columnDummyAlias = columnFormat.getColumnNameAlias();
				
				if(!StringUtils.isBlank(columnDummyAlias) && columnFormat.getDataType().equals("str")) {
					columLst = sameColumnAlias.get(columnDummyAlias);
					
					if(columLst == null) {
						columLst = new ArrayList<>();
						columLst.add(columnFormat);
						sameColumnAlias.put(columnFormat.getColumnNameAlias(), columLst);
					} else {
						columRemovable.add(columnFormat);
						columLst.add(columnFormat);
						sameColumnAlias.put(columnFormat.getColumnNameAlias(), columLst);											
					}
				}
				//--: End Concat fields
				if(fieldsParam == null) {
					fields.include(columnFormat.getColumnName());					
				}
				
				if(!StringUtils.isBlank(req.getKeyword())) {
					if(columnFormat.getDataType() != null) {
						if(columnFormat.getDataType().equals("str")) {
							multiOr.add(Criteria.where(columnFormat.getColumnName()).regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));							
						} else if(columnFormat.getDataType().equals("num")) {
							//--: Ignore right now.
						}
					} else {
						LOG.debug(columnFormat.getColumnName() + "' dataType is null");
					}
				}
			}
			
			//--: Remove Column Header
			if(columRemovable.size() > 0) {
				LOG.debug("Remove Column Header");
				columnFormats.removeAll(columRemovable);
			}
			
			Criteria[] multiOrArr = multiOr.toArray(new Criteria[multiOr.size()]);
			if(multiOrArr.length > 0) {
				criteria.orOperator(multiOrArr);				
			}
			
			//-------------------------------------------------------------------------------------
			long totalItems = 0;
			if(fieldsParam == null) {
				LOG.debug("Start Count " + NEW_TASK_DETAIL.getName() + " record");
				totalItems = template.count(query, NEW_TASK_DETAIL.getName());
				LOG.debug("End Count " + NEW_TASK_DETAIL.getName() + " record");
			}
			
			//-------------------------------------------------------------------------------------
			List<Map> taskDetails = null;
			List<ObjectId> ids = null;
			
			if(req.getSearchIds() != null) {
				ids = new ArrayList<>();
				for (String id : req.getSearchIds()) {
					ids.add(new ObjectId(id));
				}				
			}			
				
			if(!StringUtils.isBlank(req.getActionType())) {
				List<Map> find = template.find(query, Map.class, NEW_TASK_DETAIL.getName());
				List<String> taskIds = new ArrayList<>();
				
				for (Map map : find) {
					taskIds.add(String.valueOf(map.get("_id")));
				}
				
				if(req.getActionType().equals("enable")) {
					taskEnableDisable(taskIds, req.getProductId(), true);
				} else if(req.getActionType().equals("disable")) {
					taskEnableDisable(taskIds, req.getProductId(), false);
				} else if(req.getActionType().equals("remove")) {
					taskRemoveByIds(taskIds, req.getProductId());
				}
			}
			
			if(fieldsParam == null) {
				query = query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));				
			} else if(fieldsParam != null) {
				for (String field : fieldsParam) {
					query.fields().include(field.equals("user") ? SYS_OWNER_ID.getName() : field);
				}
			}
			
			if(req.getSearchIds() != null) {
				query = Query.query(Criteria.where("_id").in(ids));
			}
			
			if(StringUtils.isBlank(req.getColumnName())) {
				query.with(new Sort(SYS_OLD_ORDER.getName()));
			} else {				
				if(req.getColumnName().equals(SYS_OWNER.getName())) {
					req.setColumnName(SYS_OWNER_ID.getName());
				}
				query.with(new Sort(Direction.fromString(req.getOrder()), req.getColumnName().split(",")));
			}				
			
			LOG.debug("Start find " + NEW_TASK_DETAIL.getName());
			taskDetails = template.find(query, Map.class, NEW_TASK_DETAIL.getName());			
			LOG.debug("End find " + NEW_TASK_DETAIL.getName());
			
			if((req.getIsPgs() == null || !req.getIsPgs()) && (req.getIsNoTrace() == null || !req.getIsNoTrace())
				&& isWorkingPage && taskDetails.size() == 0 && !StringUtils.isBlank(req.getKeyword())) {
				
				LOG.debug("Start find in comment");
				Criteria criteriaComment = Criteria.where("comment").regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE));
				Query queryComment = Query.query(criteriaComment);
				queryComment.fields().include("contractNo");
				List<TraceWorkComment> comments = template.find(queryComment, TraceWorkComment.class);
				LOG.debug("End find in comment");
				
				if(comments.size() > 0) {
					LOG.debug("Start search task");
					queryComment = searchByCommentQuery(comments, productSetting.getContractNoColumnName(), req.getColumnName(), req.getOrder());
					queryComment.fields().getFieldsObject().putAll(query.getFieldsObject());
					
					if(fieldsParam == null) {
						LOG.debug("Start Count " + NEW_TASK_DETAIL.getName() + " record");
						totalItems = template.count(queryComment, NEW_TASK_DETAIL.getName());
						LOG.debug("End Count " + NEW_TASK_DETAIL.getName() + " record");
						
						queryComment = queryComment.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
					}
					
					if(totalItems > 0) {
						taskDetails = template.find(queryComment, Map.class, NEW_TASK_DETAIL.getName());
					}
					LOG.debug("Start search task");
				}
			}
			
			//-------------------------------------------------------------------------------------
			LOG.debug("Change id from ObjectId to normal ID");
			Object obj;
			String result = "";
			Date comparedAppointDate;
			Date comparedNextTimeDate;
			Date comparedTraceDate;
			CompareDateStatusConstant status;
			List<String> userIds;
			Map<String, String> userMap;
			List<Map<String, String>> userList;
			int traceStatus;
			
			for (Map map : taskDetails) {
				//--: Concat fields
				for(Entry<String, List<ColumnFormat>> entry : sameColumnAlias.entrySet()) {
					List<ColumnFormat> value = entry.getValue();
					if(value.size() < 2) continue;
					
					result = "";
					for (ColumnFormat col : value) {
						obj = map.get(col.getColumnName());
						if(!(obj instanceof String)) break;
						result += " " + obj;
						map.remove(col.getColumnName());
					}
					map.put(value.get(0).getColumnName(), result);
				}
				//--: End Concat fields
				
				map.put("id", map.get("_id").toString()); 
				map.remove("_id");
				
				userIds = (List)map.get(SYS_OWNER_ID.getName());
				
				if(userIds != null) {
					userList = MappingUtil.matchUserId(users, userIds.get(0));
					map.put(SYS_OWNER.getName(), userList);		
					
					if(userList != null && userList.size() > 0) {						
						map.put("user", userList.get(0).get("username"));		
					}
				}
				
				if(fieldsParam == null) {
					//--: Get trace status
					comparedAppointDate = (Date)map.get(SYS_APPOINT_DATE.getName());
					comparedNextTimeDate = (Date)map.get(SYS_NEXT_TIME_DATE.getName());
					comparedTraceDate = (Date)map.get(SYS_TRACE_DATE.getName());
					
					traceStatus = TaskDetailStatusUtil.getStatus(comparedAppointDate, comparedNextTimeDate);
					if(traceStatus == 0) {					
						traceStatus = TaskDetailStatusUtil.getStatusByTraceDate(comparedTraceDate, productSetting.getTraceDateRoundDay());
					}
					map.put(SYS_COMPARE_DATE_STATUS.getName(), traceStatus);
					
					if(comparedAppointDate != null && dummyDate.compareTo(comparedAppointDate) == 0) {
						map.remove(SYS_APPOINT_DATE.getName());
					}
					if(comparedNextTimeDate != null && dummyDate.compareTo(comparedNextTimeDate) == 0) {
						map.remove(SYS_NEXT_TIME_DATE.getName());
					}
					if(comparedTraceDate != null && dummyDate.compareTo(comparedTraceDate) == 0) {
						map.remove(SYS_TRACE_DATE.getName());
					}
				}
			}
			
			//-------------------------------------------------------------------------------------
			if(isAssign) {
				/*long noOwnerCount = countTaskNoOwner(template, req.getTaskFileId());
				resp.setNoOwnerCount(noOwnerCount);
				
				Map<String, Long> userTaskCount = countUserTask(template, req.getTaskFileId(), users);
				resp.setUserTaskCount(userTaskCount);*/
				
				resp.setBalanceColumn(product.getProductSetting().getBalanceColumnName());
				resp.setContractNoColumn(productSetting.getContractNoColumnName());
			} else if(isWorkingPage) {
				resp.setIsSmsEnable(productSetting.getIsSmsEnable());
				if(resp.getIsSmsEnable() != null && resp.getIsSmsEnable()) {
					resp.setSmsMessages(productSetting.getSmsMessages());
				}
			}
			//-------------------------------------------------------------------------------------
			
			resp.setUsers(users);
			resp.setHeaders(columnFormats);
			resp.setHeadersPayment(columnFormatsPayment);
			resp.setTotalItems(totalItems);
			resp.setTaskDetails(taskDetails);
			
			LOG.debug("End find");
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public TaskDetailViewCriteriaResp view(TaskDetailViewCriteriaReq req) throws Exception {
		try {
			LOG.debug("Start");
			
			LOG.debug("Get ColumnFormat");
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting prodSetting = product.getProductSetting();
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			List<ColumnFormat> columnFormatsPayment = product.getColumnFormatsPayment();
			List<GroupData> groupDatas = product.getGroupDatas();
			Map<Integer, List<ColumnFormat>> map = new HashMap<>();
			List<ColumnFormat> colFormLst;
			MergeColumnUtil mergeCol = new MergeColumnUtil();
			String balanceColumnName = prodSetting.getBalanceColumnName();
			Map<String, Object> calParams = new HashMap<>();
			calParams.put("discountFields", prodSetting.getDiscountFields());
			boolean isIgnore;
			Query query;
			
			if(!StringUtils.isBlank(req.getContractNo())) {
				query = Query.query(Criteria.where(prodSetting.getContractNoColumnName()).is(req.getContractNo()));
			} else {
				query = Query.query(Criteria.where("_id").is(req.getId()));
			}
			
			for (ColumnFormat colForm : columnFormats) {
				if(StringUtils.isNotBlank(balanceColumnName) && colForm.getColumnName().equals(balanceColumnName)) {
					calParams.put("balanceColumnName", colForm.getColumnName());
				}
				
				if(!colForm.getDetIsActive()) continue;
				
				//--: Concat fields	
				isIgnore = mergeCol.groupCol(colForm);
				//--: End Concat fields
				
				query.fields().include(colForm.getColumnName());
				
				if(!isIgnore) {
					if(map.containsKey(colForm.getDetGroupId())) {					
						colFormLst = map.get(colForm.getDetGroupId());
						colFormLst.add(colForm);
					} else {
						colFormLst = new ArrayList<>();
						colFormLst.add(colForm);
						map.put(colForm.getDetGroupId(), colFormLst);
					}
				}
			}
			
			if(StringUtils.isNotBlank(prodSetting.getDiscountColumnName())) {				
				calParams.put("balanceColumnName", prodSetting.getDiscountColumnName());
			}
			
			query.fields().include(prodSetting.getContractNoColumnName());
			query.fields().include(prodSetting.getIdCardNoColumnName());
			query.fields().include(SYS_OWNER_ID.getName());
			
			LOG.debug("Get Task");
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Map mainTask = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
			
			List<String> ownerId = (List)mainTask.get(SYS_OWNER_ID.getName());
			
			if(ownerId != null) {
				LOG.debug("Call get USERS");
				List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
				List<Map<String, String>> userList = MappingUtil.matchUserId(users, ownerId.get(0));
				mainTask.put(SYS_OWNER.getName(), userList);
			}
			
			TraceFindCriteriaResp traceResp = null;
			List<Address> addresses = null;
			
			LOG.debug("Get trace data");
			TraceFindCriteriaReq traceFindReq = new TraceFindCriteriaReq();
			traceFindReq.setCurrentPage(req.getTraceCurrentPage());
			traceFindReq.setItemsPerPage(req.getTraceItemsPerPage());
			traceFindReq.setProductId(req.getProductId());
			traceFindReq.setContractNo(String.valueOf(mainTask.get(prodSetting.getContractNoColumnName())));
			traceFindReq.setIsOldTrace(req.getIsOldTrace());
			
			traceResp = traceWorkService.find(traceFindReq);		
			traceResp.setContractNo(traceFindReq.getContractNo());
			traceResp.setIdCardNo(String.valueOf(mainTask.get(prodSetting.getIdCardNoColumnName())));
			
			LOG.debug("End get trace data");
			
			LOG.debug("Get Address data");
			AddressFindCriteriaReq addrReq = new AddressFindCriteriaReq();
			addrReq.setProductId(req.getProductId());
			addrReq.setContractNo(traceFindReq.getContractNo());
			addrReq.setIdCardNo(traceResp.getIdCardNo());
			addresses = addressService.find(addrReq);
			LOG.debug("End get Address data");
			
			//--: Concat fields
			mergeCol.matchVal(mainTask);
			//--: End Concat fields
			
			yearType(mainTask, columnFormats);
			
			TaskDetailViewCriteriaResp resp = new TaskDetailViewCriteriaResp();
			resp.setIsDisableNoticePrint(prodSetting.getIsDisableNoticePrint());
			resp.setIsHideComment(prodSetting.getIsHideComment());
			resp.setTaskDetail(mainTask);
			resp.setColFormMap(map);
			resp.setGroupDatas(groupDatas);
			resp.setTraceResp(traceResp);
			resp.setAddresses(addresses);
			resp.setCreatedByLog(prodSetting.getCreatedByLog());
			resp.setCalParams(calParams);
			resp.setTextLength(prodSetting.getTextLength());
			resp.setUserEditable(prodSetting.getUserEditable());
			resp.setPayTypes(prodSetting.getPayTypes());
			resp.setShowUploadDoc(prodSetting.getShowUploadDoc());
			resp.setSeizure(prodSetting.getSeizure());
			resp.setIsDisableBtnShow(prodSetting.getIsDisableBtnShow());
			
			LOG.debug("Call getRelatedData");
			Map<String, RelatedData> relatedData = getRelatedData(template, addrReq.getContractNo(), addrReq.getIdCardNo());				
			resp.setRelatedData(relatedData);
						
			LOG.debug("Call getGetPayment");
			getPayment(template,
					   traceFindReq.getContractNo(), 
					   columnFormatsPayment, 
					   resp, req.getCurrentPagePayment(), 
					   req.getItemsPerPagePayment(), 
					   prodSetting);
			
			LOG.debug("Call Comment");
			TraceCommentCriteriaReq commentReq = new TraceCommentCriteriaReq();
			commentReq.setContractNo(addrReq.getContractNo());
			commentReq.setProductId(req.getProductId());
			TraceWorkComment comment = traceWorkService.findComment(commentReq);
			if(comment != null) {
				resp.setComment(comment.getComment());				
			}
			
			LOG.debug("End");
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public TaskDetailViewCriteriaResp getTaskDetailToNotice(TaskDetailViewCriteriaReq req) {
		try {
			LOG.debug("Start");
			
			LOG.debug("Get ColumnFormat");
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			Query query = Query.query(Criteria.where("_id").in(req.getIds()));
			query.fields().include(SYS_OWNER_ID.getName()).include("LOAN_TYPE"); // LOAN_TYPE for KYS only
			
			for (ColumnFormat colForm : columnFormats) {
				if(!colForm.getDetIsActive()) continue;
				
				query.fields().include(colForm.getColumnName());
			}
			
			LOG.debug("Get Task");
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			List<Map> mainTask = template.find(query, Map.class, NEW_TASK_DETAIL.getName());
			
			for (Map map : mainTask) {
				List<String> owner = (List)map.get(SYS_OWNER_ID.getName());
				
				if(owner != null) {
					String userId = owner.get(0);
					
					LOG.debug("find user");
					Users user = userRepository.findOne(userId);
					
					map.put("owner_fullname", StringUtils.trimToEmpty(user.getFirstName()) + " " + StringUtils.trimToEmpty(user.getLastName()));
					map.put("owner_tel", StringUtils.stripToEmpty(user.getPhoneNumber()));
					map.put("owner_tel_ext", StringUtils.stripToEmpty(user.getPhoneExt()));
				}
			}
			
			TaskDetailViewCriteriaResp resp = new TaskDetailViewCriteriaResp();
			resp.setTaskDetails(mainTask);
			
			LOG.debug("End");
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void taskAssigningBySelected(TaskDetailCriteriaReq req) {
		try {
			LOG.debug("Start taskAssigningBySelected");
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			
			Criteria criteria = Criteria.where("_id").in(req.getTaskIds());
			Query query = Query.query(criteria).with(new Sort(Sort.Direction.DESC, productSetting.getBalanceColumnName()));
			
			List<Map> taskDetails = template.find(query, Map.class, NEW_TASK_DETAIL.getName());
			assign(req, taskDetails);
			
			LOG.debug("End taskAssigningBySelected");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void taskAssigningWhole(TaskDetailCriteriaReq req) throws Exception {
		try {
			LOG.debug("Start taskAssigningBySelected");
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			
			TaskTypeConstant taskType = TaskTypeConstant.findById(req.getTaskType());
			LOG.debug(taskType);
			Query query;
			Criteria criteria;
			
			switch (taskType) {
			case EMPTY:
				criteria = Criteria.where(SYS_OWNER_ID.getName()).is(null);
				
				if(!StringUtils.isBlank(req.getTaskFileId())) {
					criteria.and(SYS_FILE_ID.getName()).is(req.getTaskFileId());
				}
				query = Query.query(criteria).with(new Sort(Sort.Direction.DESC, productSetting.getBalanceColumnName()));
				break;
			case TRANSFER:
				List<String> userIds = req.getTransferUsernames();
				criteria = Criteria.where(SYS_OWNER_ID.getName() + ".0").in(userIds);
				
				if(!StringUtils.isBlank(req.getTaskFileId())) {
					criteria.and(SYS_FILE_ID.getName()).is(req.getTaskFileId());
				}
				
				query = Query.query(criteria).with(new Sort(Sort.Direction.DESC, productSetting.getBalanceColumnName()));
				break;

			default: throw new Exception("TaskType not found.");
			}
			
			List<Map> taskDetails = template.find(query, Map.class, NEW_TASK_DETAIL.getName());
			assign(req, taskDetails);
			
			LOG.debug("End taskAssigningBySelected");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void assign(TaskDetailCriteriaReq req, List<Map> taskDetails) {
		try {
			LOG.debug("Start Assign");
			
			int userNum = req.getUsernames().size();
			LOG.debug("Num of collectors to be assigned: " + userNum);
			int count = 0;
			
			AssignMethodConstant method = AssignMethodConstant.findById(req.getMethodId());
			LOG.debug(method);
			List<Integer> index;
			
			if(method == AssignMethodConstant.RANDOM) {
				index = RandomUtil.random(userNum);				
			} else {
				index = RandomUtil.order(userNum);
			}
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			List<String> owners;
			Double calColVal;
			
			for (Map map : taskDetails) {
				calColVal = (Double)map.get(productSetting.getBalanceColumnName());
				if(calColVal == null) continue;
				
				owners = (List<String>)map.get(SYS_OWNER_ID.getName());
				if(owners == null) owners = new ArrayList<>();
				
				if(count == userNum) {
					if(method == AssignMethodConstant.RANDOM) {
						index = RandomUtil.random(userNum);						
					}
					count = 0;					
				}
				
				owners.add(0, req.getUsernames().get(index.get(count)).get("id"));
				map.put(SYS_OWNER_ID.getName(), owners);
				template.save(map, NEW_TASK_DETAIL.getName());
				
				count++;
			}
			
			LOG.debug("End Assign");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public UpdateTaskIsActiveCriteriaResp updateTaskIsActive(UpdateTaskIsActiveCriteriaReq req) {
		UpdateTaskIsActiveCriteriaResp resp = new UpdateTaskIsActiveCriteriaResp();
		
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Criteria criteria;
			
			for (IsActiveModel isActive : req.getIsActives()) {
				criteria = Criteria.where("_id").is(isActive.getId());
				template.updateFirst(Query.query(criteria), Update.update(SYS_IS_ACTIVE.getName(), new IsActive(isActive.getStatus(), "")), NEW_TASK_DETAIL.getName());						
			}
			
			
			criteria = Criteria.where(SYS_FILE_ID.getName()).is(req.getTaskFileId()).and(SYS_OWNER_ID.getName()).is(null).and(SYS_IS_ACTIVE.getName() + ".status").is(true);
			long noOwnerCount = template.count(Query.query(criteria), NEW_TASK_DETAIL.getName());
			LOG.debug("rowNum of don't have owner yet: " + noOwnerCount);
			
			resp.setNoOwnerCount(noOwnerCount);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Map<String, Object> uploadUpload(InputStream uploadedInputStream, 
							 FormDataContentDisposition fileDetail, 
							 String productId, 
							 String taskFileId,
							 Boolean isConfirmImport, 
							 List<YearType> yearT) throws Exception {
		
		Workbook workbook = null;
		try {
			Date date = Calendar.getInstance().getTime();
			
			LOG.debug("Get Filename");
			FileDetail fd = FileUtil.getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			if(fd.fileExt.equals(".xlsx")) {
				workbook = new XSSFWorkbook(uploadedInputStream);
			} else if(fd.fileExt.equals(".xls")) {
				workbook = new HSSFWorkbook(uploadedInputStream);
			} else {
				throw new CustomerException(5000, "Filetype not match");
			}
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			
			if(StringUtils.isBlank(productSetting.getContractNoColumnName())) {
				throw new Exception("ContractNoColumnName is null");
			}
			
			String userCol = "user";
			Sheet sheet = workbook.getSheetAt(0);
			
			LOG.debug("Call uploadAssing");
			uploadAssing(sheet, productId, taskFileId, productSetting, userCol);
			
			LOG.debug("Call uploadUpdate");
			Map<String, Object> colData = uploadData(sheet, productId, taskFileId, productSetting, userCol, isConfirmImport, yearT, product.getColumnFormats());
			
			return colData;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
		}
	}
	
	private void uploadAssing(Sheet sheet, String productId, String taskFileId, ProductSetting productSetting, String userCol) throws Exception {
		try {
			String contractNoCol = productSetting.getContractNoColumnName();
			String contractNoColPay = productSetting.getContractNoColumnNamePayment();
			UpdateByLoad assignByLoad = new UpdateByLoad();
			
			LOG.debug("Call getHeaderAssign");
			Map<String, Integer> headerIndex = assignByLoad.getHeaderAssign(sheet, contractNoCol, userCol);
			
			if(headerIndex.size() == 0 || !headerIndex.containsKey(userCol.toUpperCase()) || !headerIndex.containsKey(contractNoCol.toUpperCase())) {
				return;
			}
			
			LOG.debug("Call getBodyAssign");
			Map<String, List<String>> assignVal = assignByLoad.getBodyAssign(sheet, headerIndex, contractNoCol, userCol);
			
			if(assignVal.size() == 0) {
				return;
			}
			
			LOG.debug("Find all Users");
			List<Users> users = templateCenter.find(Query.query(Criteria.where("username").in(assignVal.keySet())), Users.class);
			if(users.size() == 0) {
				throw new Exception("Not found users");
			}
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			LOG.debug("Call assign");
			assignByLoad.assign(users, assignVal, template, contractNoCol, contractNoColPay, taskFileId);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private Map<String, Object> uploadData(Sheet sheet, String productId, String taskFileId, 
							ProductSetting productSetting, String userCol, 
							Boolean isConfirmImport, List<YearType> yearType,
							List<ColumnFormat> columnFormats) throws Exception {
		try {
			UpdateByLoad assignByLoad = new UpdateByLoad();
			
			LOG.debug("Call getHeaderAssign");
			Map<String, Integer> headerIndex = assignByLoad.getHeaderUpdate(sheet, userCol);
			
			if(headerIndex.size() == 0 || headerIndex.keySet().size() < 2) {
				return null;
			}
			
			ColumnFormat isAcitve = new ColumnFormat(SYS_IS_ACTIVE.getName(), true);
			columnFormats.add(isAcitve);
			
			if((isConfirmImport == null || !isConfirmImport)) {
				List<ColumnFormat> colDateTypes = ImportExcel.getColDateType(headerIndex, columnFormats);
				List<String> colNotFounds = ImportExcel.getColNotFound(headerIndex, columnFormats);
				Map<String, Object> colData = new HashMap<>();
				colData.put("colDateTypes", colDateTypes);
				colData.put("colNotFounds", colNotFounds);
				
				if(colDateTypes.size() > 0 || colNotFounds.size() > 0) return colData;
			}
			
			LOG.debug("Call getBodyUpdate");
			List<Map<String, Object>> updateVal = assignByLoad.getBodyUpdate(sheet, headerIndex, columnFormats, yearType, excelUtil);
			
			if(updateVal.size() == 0) {
				return null;
			}
			
			isAcitve.setIsActive(false);
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			List<ColumnFormat> activeCols = getColumnFormatsActive(columnFormats, false);
			
			LOG.debug("Call assign");
			assignByLoad.update(updateVal, template, productSetting, taskFileId, activeCols);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
		return null;
	}
	
	public void taskEnableDisable(List<String> ids, String productId, boolean status) throws Exception {
		try {
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			template.updateMulti(Query.query(Criteria.where("_id").in(ids)), Update.update(SYS_IS_ACTIVE.getName(), new IsActive(status, "")), NEW_TASK_DETAIL.getName());		
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateTaskData(TaskUpdateDetailCriteriaReq req) throws Exception {
		try {
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Query query;
			Update update;
			
			if(StringUtils.isBlank(req.getRelatedMenuId())) {
				Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
				query = Query.query(Criteria.where("_id").is(req.getId()));
				
				LOG.debug("Start call getUpdateVal");
				update = getUpdateVal(req);
				LOG.debug("End call getUpdateVal");
				
				LOG.debug("Update to empty");
				List<String> cols = getColNameSameAlias(product.getColumnFormats(), req.getColumnNameAlias());
				if(cols != null) {					
					Update updateValToEmpty = getUpdateValToEmpty(cols);
					if(updateValToEmpty != null) {						
						template.updateFirst(query, updateValToEmpty, NEW_TASK_DETAIL.getName());
					}
				}
				
				LOG.debug("Update value");
				template.updateFirst(query, update, NEW_TASK_DETAIL.getName());		
			} else {
				ImportMenu menu = template.findOne(Query.query(Criteria.where("id").is(req.getRelatedMenuId())), ImportMenu.class);
				ImportOthersSetting menuSetting = menu.getSetting();
				
				if(menuSetting == null) throw new Exception("menuSetting is null");
				
				String childContractNoColumnName = menuSetting.getContractNoColumnName();
				String childIdCardNoColumnName = menuSetting.getIdCardNoColumnName();
				
				Criteria criteria = new Criteria();
				List<Criteria> multiOr = new ArrayList<>();
				
				if(!StringUtils.isBlank(childContractNoColumnName)) {			
					multiOr.add(Criteria.where(childContractNoColumnName).is(req.getContractNo()));
				}
				if(!StringUtils.isBlank(childIdCardNoColumnName)) {
					multiOr.add(Criteria.where(childIdCardNoColumnName).is(req.getIdCardNo()));
				}				
				
				Criteria[] multiOrArr = multiOr.toArray(new Criteria[multiOr.size()]);
				criteria.orOperator(multiOrArr);
				query = Query.query(criteria);
				
				LOG.debug("Start call getUpdateVal");
				update = getUpdateVal(req);
				LOG.debug("End call getUpdateVal");
				
				LOG.debug("Update to empty");
				List<String> cols = getColNameSameAlias(menu.getColumnFormats(), req.getColumnNameAlias());
				if(cols != null) {					
					Update updateValToEmpty = getUpdateValToEmpty(cols);
					if(updateValToEmpty != null) {						
						template.updateFirst(query, updateValToEmpty, req.getRelatedMenuId());
					}
				}
				
				template.updateFirst(query, update, req.getRelatedMenuId());		
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void taskRemoveByIds(List<String> ids, String productId) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			//---: Query Data
			/*LOG.debug("Find product");
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			
			List<String> contractNoVals = new ArrayList<>();
			List<String> idCardVals = new ArrayList<>();
			ProductSetting productSetting = product.getProductSetting();
			String contractNoColumn = productSetting.getContractNoColumnName();
			String contractNoColumnPayment = productSetting.getContractNoColumnNamePayment();
			String idCardColumn = productSetting.getIdCardNoColumnName();*/
			
			Query query = Query.query(Criteria.where("_id").in(ids));
//			query.fields().include(contractNoColumn).include(idCardColumn);
			
			//---: Query Data
			/*LOG.debug("Find newTaskDetail");
			List<Map> tasks = template.find(query, Map.class, NEW_TASK_DETAIL.getName());
			
			for (Map map : tasks) {
				contractNoVals.add(map.get(contractNoColumn).toString());
				idCardVals.add(map.get(idCardColumn).toString());
			}*/
			
			//---------: Remove others data
//			LOG.debug("Remove allRelated");
//			RemoveRelatedDataUtil.allRelated(template, contractNoVals, idCardVals, contractNoColumnPayment);
			
			LOG.debug("Remove newTaskDetail");
			template.remove(query, NEW_TASK_DETAIL.getName());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateTags(TagsCriteriaReq req) {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Update update = Update.update(SYS_TAGS.getName(), req.getTags());
			template.updateFirst(Query.query(Criteria.where("_id").is(req.getId())), update, NEW_TASK_DETAIL.getName());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats, boolean isAssign) {
		if(columnFormats == null) return null;
		
		List<ColumnFormat> result = new ArrayList<>();
		
		if(isAssign) {
			ColumnFormat isActive = new ColumnFormat(SYS_IS_ACTIVE.getName(), true);
			isActive.setColumnNameAlias("สถานะ");
			isActive.setDataType(SYS_IS_ACTIVE.getName());
			result.add(isActive);			
		}
		
		for (ColumnFormat colFormat : columnFormats) {
			if(colFormat.getIsActive()) {
				result.add(colFormat);
			}
		}
		
		return result;
	}
	
	private long countTaskNoOwner(MongoTemplate template, String taskFileId) {
		try {
			LOG.debug("Start");
			Criteria criteria;
			
			if(StringUtils.isBlank(taskFileId)) {
				criteria = new Criteria();
			} else {				
				criteria = Criteria.where(SYS_FILE_ID.getName()).is(taskFileId);
			}
			
			criteria.and(SYS_OWNER_ID.getName()).is(null).and(SYS_IS_ACTIVE.getName() + ".status").is(true);
			long noOwnerCount = template.count(Query.query(criteria), NEW_TASK_DETAIL.getName());
			LOG.debug("rowNum of don't have owner yet: " + noOwnerCount);
			LOG.debug("End");
			return noOwnerCount;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private Map<String, Long> countUserTask(MongoTemplate template, String taskFileId, List<Users> users) {
		try {
			LOG.debug("Start");
			Map<String, Long> userTaskCount = new HashMap<>();
			Criteria criteria;
			
			for (Users u : users) {
				
				if(StringUtils.isBlank(taskFileId)) {
					criteria = new Criteria();
				} else {				
					criteria = Criteria.where(SYS_FILE_ID.getName()).is(taskFileId);
				}
				
				criteria
				.and(SYS_IS_ACTIVE.getName() + ".status").is(true)
				.and(SYS_OWNER_ID.getName() + ".0").is(u.getId());
				userTaskCount.put(u.getUsername(), template.count(Query.query(criteria), NEW_TASK_DETAIL.getName()));
			}
			LOG.debug("End");
			return userTaskCount;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private Map<String, RelatedData> getRelatedData(MongoTemplate template, String mainContractNo, String mainIdCardNo) {
		LOG.debug("Start");
		
		try {
			if(StringUtils.isBlank(mainContractNo) && StringUtils.isBlank(mainIdCardNo)) {
				LOG.debug("Both Ref. ids are empty");
				return null;
			}
			
			Query relatedDataQuery;
			List<ImportMenu> importMenus = template.find(Query.query(Criteria.where("enabled").is(true)), ImportMenu.class);
			Map<String, RelatedData> relatedData = new LinkedHashMap<>();
			Map<Integer, List<ColumnFormat>> othersMap;
			String childIdCardNoColumnName, childContractNoColumnName;
			ImportOthersSetting importOthersSetting;
			List<ColumnFormat> importMenuColForm;
			List<GroupData> importMenuGroupDatas;
			List<ColumnFormat> othersColFormLst;
			MergeColumnUtil mergeCol;
			List<Criteria> multiOr;
			Criteria[] multiOrArr;
			List<Map> dataMap;
			Criteria criteria;
			RelatedData data;
			boolean isIgnore;
			Map dataMapDummy;
			
			if(importMenus != null) {
				for (ImportMenu importMenu : importMenus) {					
					importMenuColForm = importMenu.getColumnFormats();
					importMenuGroupDatas = importMenu.getGroupDatas();
					importOthersSetting = importMenu.getSetting();
					
					if(importOthersSetting == null) continue;
					
					data = new RelatedData();
					othersMap = new HashMap<>();
						
					childContractNoColumnName = importOthersSetting.getContractNoColumnName();
					childIdCardNoColumnName = importOthersSetting.getIdCardNoColumnName();
					criteria = new Criteria();
					multiOr = new ArrayList<>();
					
					if(!StringUtils.isBlank(childContractNoColumnName)) {			
						multiOr.add(Criteria.where(childContractNoColumnName).is(mainContractNo));
					}
					if(!StringUtils.isBlank(childIdCardNoColumnName)) {
						multiOr.add(Criteria.where(childIdCardNoColumnName).is(mainIdCardNo));
					}
					
					if(multiOr.size() == 0) continue;
					
					multiOrArr = multiOr.toArray(new Criteria[multiOr.size()]);
					criteria.orOperator(multiOrArr);
					
					relatedDataQuery = Query.query(criteria);
					relatedDataQuery.with(new Sort(Sort.Direction.DESC, SYS_CREATED_DATE_TIME.getName()));
					mergeCol = new MergeColumnUtil();
					
					for (ColumnFormat colForm : importMenuColForm) {
						if(!colForm.getDetIsActive()) continue;
						
						//--: Concat fields
						isIgnore = mergeCol.groupCol(colForm);
						//--: End Concat fields
						
						relatedDataQuery.fields().include(colForm.getColumnName());
						
						if(!isIgnore) {
							if(othersMap.containsKey(colForm.getDetGroupId())) {					
								othersColFormLst = othersMap.get(colForm.getDetGroupId());
								othersColFormLst.add(colForm);
							} else {
								othersColFormLst = new ArrayList<>();
								othersColFormLst.add(colForm);
								othersMap.put(colForm.getDetGroupId(), othersColFormLst);
							}
						}
					}
					
					dataMap = template.find(relatedDataQuery, Map.class, importMenu.getId());
					
					if(dataMap.size() == 0) {
						dataMap.add(new HashMap<>());
					} else {
						dataMapDummy = dataMap.get(0);
						
						//--: Concat fields
						mergeCol.matchVal(dataMapDummy);
						//--: End Concat fields
						
						dataMap.clear();
						dataMap.add(dataMapDummy);
					}
					
					data.setOthersData(dataMap);
					data.setOthersColFormMap(othersMap);
					data.setOthersGroupDatas(importMenuGroupDatas);
					relatedData.put(importMenu.getId(), data);
				}
			}
			
			LOG.debug("End");
			return relatedData;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void getPayment(MongoTemplate template,
			String contractNo, 
			List<ColumnFormat> columnFormatsPayment, 
			TaskDetailViewCriteriaResp resp,
			int currentPage, int itemsPerPage,
			ProductSetting prodSetting) {
		
		try {
			String balanceColumnName = prodSetting.getBalanceColumnName();
			String conNoColPayment = prodSetting.getContractNoColumnNamePayment();
			String sortingColPayment = prodSetting.getSortingColumnNamePayment();
			   
			if(columnFormatsPayment == null || StringUtils.isBlank(conNoColPayment)) return ;
			
			Criteria criteria = Criteria.where(conNoColPayment).is(contractNo);
			Query paymentQuery = Query.query(criteria);
			List<ColumnFormat> sumFields = new ArrayList<>();
			
			for (ColumnFormat colForm : columnFormatsPayment) {
				if(!colForm.getIsActive()) continue;
				
				if(colForm.getIsSum() != null && colForm.getIsSum()) {
					sumFields.add(colForm);
				}
				
				paymentQuery.fields().include(colForm.getColumnName());
			}
			
			String paymentRules = prodSetting.getPaymentRules();
			if(paymentRules != null) {		
				paymentQuery.fields().include(balanceColumnName);
				paymentQuery.fields().include("LATEST_" + balanceColumnName);
				String rules[] = paymentRules.split("\\r?\\n");
				
				if(rules != null) {
					String expression[];
					for (String rule : rules) {
						expression = rule.split("=");
						paymentQuery.fields().include(expression[1]);
					}
				}
			}
			
			GroupOperation groupOperation = Aggregation.group().count().as("totalItems");
			String fieldSuffix = "_Sum_Sys";
			
			for (ColumnFormat field : sumFields) {
				groupOperation = groupOperation.sum(field.getColumnName()).as(field.getColumnName() + fieldSuffix);
			}
			Aggregation aggCount = Aggregation.newAggregation(
					Aggregation.match(criteria),
					groupOperation
			);
			
			AggregationResults<Map> aggregate = template.aggregate(aggCount, NEW_PAYMENT_DETAIL.getName(), Map.class);
			Map aggCountResult = aggregate.getUniqueMappedResult();
			if(aggCountResult == null) {
				LOG.debug("Not found data");
				resp.setPaymentTotalItems(Long.valueOf(0));
				return;
			}
			
			List<SumPayment> sums = new ArrayList<>();
			SumPayment sumPayment;
			String key;
			
			for (ColumnFormat field : sumFields) {
				try {
					key = field.getColumnName() + fieldSuffix;
					sumPayment = new SumPayment();
					sumPayment.setColumnName(StringUtils.isBlank(field.getColumnNameAlias()) ? field.getColumnName() : field.getColumnNameAlias());
					sumPayment.setValue(Double.valueOf(aggCountResult.get(key).toString()));
					sums.add(sumPayment);					
				} catch (Exception e) {
					LOG.error(e.toString());
				}
			}
			
			resp.setPaymentSums(sums);
			resp.setPaymentTotalItems(((Integer)aggCountResult.get("totalItems")).longValue());
			
			paymentQuery.with(new PageRequest(currentPage - 1, itemsPerPage));
			paymentQuery.with(new Sort(Direction.DESC, StringUtils.isBlank(sortingColPayment) ? SYS_CREATED_DATE_TIME.getName() : sortingColPayment));
			
			List<Map> paymentDetails = template.find(paymentQuery, Map.class, NEW_PAYMENT_DETAIL.getName());
			resp.setPaymentDetails(paymentDetails);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private Update getUpdateVal(TaskUpdateDetailCriteriaReq req) {
		Update update;
		
		if(req.getValueDate() != null) {
			update = Update.update(req.getColumnName(), req.getValueDate());
		} else if(req.getDataType().equals("num")) {
			update = Update.update(req.getColumnName(), Double.parseDouble(req.getValue()));
		} else {
			update = Update.update(req.getColumnName(), req.getValue());
		}
		
		return update;
	}
	
	private Update getUpdateValToEmpty(List<String> cols) {
		if(cols != null && cols.size() > 1) {
			Update update = new Update(); 
			
			for (String col : cols) {
				update.set(col, null);
			}
			return update;
		}
		return null;
	}
	
	private void yearType(Map<String, Object> val, List<ColumnFormat> columnFormats) {
		if(val == null) return;
		
		Object valObj;
		Date valDate;
		
		for (ColumnFormat colForm : columnFormats) {
			if(!colForm.getDetIsActive()) continue;
			
			if(colForm.getIsBuddhismYear() == null || !colForm.getIsBuddhismYear()) continue;
				
			valObj = val.get(colForm.getColumnName());
			
			if(!(valObj instanceof Date)) continue;
			
			valDate = (Date)valObj;
			valDate.setYear(valDate.getYear() + 543);
		}
	}
	
	private List<String> getColNameSameAlias(List<ColumnFormat> columnFormats, String columnNameAlias) {
		if(!StringUtils.isBlank(columnNameAlias)) {
			List<String> cols = new ArrayList<>();
			for (ColumnFormat colForm : columnFormats) {
				if(StringUtils.isBlank(colForm.getColumnNameAlias())) continue;
				
				if(colForm.getColumnNameAlias().equals(columnNameAlias)) {
					cols.add(colForm.getColumnName());
				}
			}					
			return cols;
		}
		return null;
	}
	
	private Query searchByCommentQuery(List<TraceWorkComment> comments, String contractCol, String columnName, String order) {
		List<String> contracts = new ArrayList<>();
		for (TraceWorkComment comment : comments) {
			contracts.add(comment.getContractNo());
		}
		
		Query queryOnComment = Query.query(Criteria.where(contractCol).in(contracts).and(SYS_IS_ACTIVE.getName() + ".status").is(true));
		
		if(StringUtils.isBlank(columnName)) {
			queryOnComment.with(new Sort(SYS_OLD_ORDER.getName()));
		} else {				
			if(columnName.equals(SYS_OWNER.getName())) {
				columnName = SYS_OWNER_ID.getName();
			}
			queryOnComment.with(new Sort(Direction.fromString(order), columnName.split(",")));
		}
		return queryOnComment;
	}
	
	private Query taskIdQuery(Criteria criteria, String columnName, String order) {
		Query queryId = Query.query(criteria);
		queryId.fields().include("id");
		
		if(StringUtils.isBlank(columnName)) {
			queryId.with(new Sort(SYS_OLD_ORDER.getName()));
		} else {			
			if(columnName.equals(SYS_OWNER.getName())) {
				columnName = SYS_OWNER_ID.getName();
			}
			queryId.with(new Sort(Direction.fromString(order), columnName.split(",")));
		}
		return queryId;
	}
	
}
