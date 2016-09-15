package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_APPOINT_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_COMPARE_DATE_STATUS;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_IS_ACTIVE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_NEXT_TIME_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OLD_ORDER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;

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
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.bussiness.AssignByLoad;
import com.may.ple.backend.constant.AssignMethodConstant;
import com.may.ple.backend.constant.CompareDateStatusConstant;
import com.may.ple.backend.constant.TaskTypeConstant;
import com.may.ple.backend.criteria.AddressFindCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailCriteriaResp;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaResp;
import com.may.ple.backend.criteria.TraceFindCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaResp;
import com.may.ple.backend.criteria.UpdateTaskIsActiveCriteriaReq;
import com.may.ple.backend.criteria.UpdateTaskIsActiveCriteriaResp;
import com.may.ple.backend.criteria.UserByProductCriteriaResp;
import com.may.ple.backend.entity.Address;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.GroupData;
import com.may.ple.backend.entity.ImportMenu;
import com.may.ple.backend.entity.ImportOthersSetting;
import com.may.ple.backend.entity.IsActive;
import com.may.ple.backend.entity.NewTaskFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.model.IsActiveModel;
import com.may.ple.backend.model.RelatedData;
import com.may.ple.backend.repository.UserRepository;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.RandomUtil;

@Service
public class TaskDetailService {
	private static final Logger LOG = Logger.getLogger(TaskDetailService.class.getName());
	private DbFactory dbFactory;
	private UserAction userAct;
	private MongoTemplate templateCenter;
	private UserRepository userRepository;
	private TraceWorkService traceWorkService;
	private AddressService addressService;	
	
	@Autowired
	public TaskDetailService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct, UserRepository userRepository, TraceWorkService traceWorkService, AddressService addressService) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.userAct = userAct;
		this.userRepository = userRepository;
		this.traceWorkService = traceWorkService;
		this.addressService = addressService;
	}
	
	public TaskDetailCriteriaResp find(TaskDetailCriteriaReq req) throws Exception {
		try {
			LOG.debug("Start find");
			Date date = new Date();
			TaskDetailCriteriaResp resp = new TaskDetailCriteriaResp();
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
			
			if(columnFormats == null) return resp;
			LOG.debug("Before size: " + columnFormats.size());
			columnFormats = getColumnFormatsActive(columnFormats, isAssign);
			LOG.debug("After size: " + columnFormats.size());
			
			columnFormatsPayment = getColumnFormatsActive(columnFormatsPayment, false);
			
			//-------------------------------------------------------------------------------------
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Criteria criteria;
			
			if(StringUtils.isBlank(req.getTaskFileId())) {
				Query queryFile = Query.query(Criteria.where("enabled").is(true));
				queryFile.fields().include("id");
				List<NewTaskFile> files = template.find(queryFile, NewTaskFile.class);
				List<String> fileIds = new ArrayList<>();
				for (NewTaskFile file : files) {
					fileIds.add(file.getId());
				}
				
				criteria = Criteria.where(SYS_FILE_ID.getName()).in(fileIds);
			} else {				
				criteria = Criteria.where(SYS_FILE_ID.getName()).is(req.getTaskFileId());
			}
			
			if(req.getIsActive() != null) {
				criteria.and(SYS_IS_ACTIVE.getName() + ".status").is(req.getIsActive());
			}
			
			//------------------------------------------------------------------------------------------------------
			if(!StringUtils.isBlank(req.getOwner())) {
				if(req.getOwner().equals("-1")) {
					criteria.and(SYS_OWNER.getName()).is(null);
				} else {
					criteria.and(SYS_OWNER.getName() + ".0.username").is(req.getOwner());										
				}
			}
			//-------------------------------------------------------------------------------------------------------
			if(isRlatedData) {
				if(productSetting != null) {					
					criteria.and(productSetting.getIdCardNoColumnName()).is(req.getIdCardNo());
				}
			}
			
			//-------------------------------------------------------------------------------------
			Query query = Query.query(criteria);
			Field fields = query.fields();
			
			//--: Include These fields alway because have to use its value.
			fields.include(SYS_OWNER.getName());
			fields.include(SYS_APPOINT_DATE.getName());
			fields.include(SYS_NEXT_TIME_DATE.getName());
			
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
				
				fields.include(columnFormat.getColumnName());
				
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
			LOG.debug("Start Count " + NEW_TASK_DETAIL.getName() + " record");
			long totalItems = template.count(query, NEW_TASK_DETAIL.getName());
			LOG.debug("End Count " + NEW_TASK_DETAIL.getName() + " record");
			
			//-------------------------------------------------------------------------------------
			if(!isWorkingPage) {
				query = query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			}
			
			if(StringUtils.isBlank(req.getColumnName())) {
				query.with(new Sort(SYS_OLD_ORDER.getName()));
			} else {				
				query.with(new Sort(Direction.fromString(req.getOrder()), req.getColumnName()));
			}
			
			LOG.debug("Start find " + NEW_TASK_DETAIL.getName());
			List<Map> taskDetails = template.find(query, Map.class, NEW_TASK_DETAIL.getName());			
			LOG.debug("End find " + NEW_TASK_DETAIL.getName());
			
			//-------------------------------------------------------------------------------------
			LOG.debug("Change id from ObjectId to normal ID");
			Object obj;
			String result = "";
			Date comparedAppointDate;
			Date comparedNextTimeDate;
			CompareDateStatusConstant status;
			
			for (Map map : taskDetails) {
				//--: Concat fields
				for(Entry<String, List<ColumnFormat>> entry : sameColumnAlias.entrySet()) {
					List<ColumnFormat> value = entry.getValue();
					if(value.size() < 2) continue;
					
					result = "";
					for (ColumnFormat col : value) {
						obj = map.get(col.getColumnName());
						if(!(obj instanceof String)) break;
						result += obj;
						map.remove(col.getColumnName());
					}
					map.put(value.get(0).getColumnName(), result);
				}
				//--: End Concat fields
				
				map.put("id", map.get("_id").toString()); 
				map.remove("_id");
				
				//--: Make status of date.
//				isAppointDate = true;
				status = CompareDateStatusConstant.NORMAL;
				comparedAppointDate = (Date)map.get(SYS_APPOINT_DATE.getName());
				comparedNextTimeDate = (Date)map.get(SYS_NEXT_TIME_DATE.getName());
				
				if(comparedAppointDate != null) {
					if(DateUtils.isSameDay(date, comparedAppointDate)) {
						status = CompareDateStatusConstant.TODAY_APPOINT_DATE;
					} else if(date.after(comparedAppointDate)) {
						status = CompareDateStatusConstant.OVER_DATE;
					} else if(comparedNextTimeDate != null){
						if(DateUtils.isSameDay(date, comparedNextTimeDate)) {
							status = CompareDateStatusConstant.TODAY_NEXT_TIME_DATE;
						} else if(date.after(comparedNextTimeDate)) {
							status = CompareDateStatusConstant.OVER_DATE;
						}
					}
				} else if(comparedNextTimeDate != null){
					if(DateUtils.isSameDay(date, comparedNextTimeDate)) {
						status = CompareDateStatusConstant.TODAY_NEXT_TIME_DATE;
					} else if(date.after(comparedNextTimeDate)) {
						status = CompareDateStatusConstant.OVER_DATE;
					}
				}
				map.put(SYS_COMPARE_DATE_STATUS.getName(), status.getStatus());
			}
			
			//-------------------------------------------------------------------------------------
			LOG.debug("Call get USERS");
			UserByProductCriteriaResp userResp = userAct.getUserByProductToAssign(req.getProductId());
			if(isAssign) {
				long noOwnerCount = countTaskNoOwner(template, req.getTaskFileId());
				resp.setNoOwnerCount(noOwnerCount);
				
				Map<String, Long> userTaskCount = countUserTask(template, req.getTaskFileId(), userResp);
				resp.setUserTaskCount(userTaskCount);
				
				if(product.getProductSetting() != null) {		
					resp.setBalanceColumn(product.getProductSetting().getBalanceColumnName());
				}
			}
			//-------------------------------------------------------------------------------------
			
			resp.setUsers(userResp.getUsers());
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
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			List<ColumnFormat> columnFormatsPayment = product.getColumnFormatsPayment();
			List<GroupData> groupDatas = product.getGroupDatas();
			Map<Integer, List<ColumnFormat>> map = new HashMap<>();
			List<ColumnFormat> colFormLst;
			Map<String, List<ColumnFormat>> sameColumnAlias = new HashMap<>();
			List<ColumnFormat> columRemovable = new ArrayList<>();
			String columnDummyAlias = "";
			List<ColumnFormat> columLst;
			Query query = Query.query(Criteria.where("_id").is(req.getId()));
			boolean isIgnore = false;
			
			for (ColumnFormat colForm : columnFormats) {
				if(!colForm.getDetIsActive()) continue;
				
				//--: Concat fields
				columnDummyAlias = colForm.getColumnNameAlias();
				
				if(!StringUtils.isBlank(columnDummyAlias) && colForm.getDataType().equals("str")) {
					columLst = sameColumnAlias.get(columnDummyAlias);
					
					if(columLst == null) {
						columLst = new ArrayList<>();
						columLst.add(colForm);
						sameColumnAlias.put(colForm.getColumnNameAlias(), columLst);
					} else {
						isIgnore = true;
						columRemovable.add(colForm);
						columLst.add(colForm);
						sameColumnAlias.put(colForm.getColumnNameAlias(), columLst);											
					}
				}
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
				isIgnore = false;
			}
			
			ProductSetting prodSetting = product.getProductSetting();
			
			if(prodSetting == null) {
				throw new Exception("Product Setting is null");
			}
			query.fields().include(prodSetting.getContractNoColumnName());
			query.fields().include(prodSetting.getIdCardNoColumnName());
			
			LOG.debug("Get Task");
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Map mainTask = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
			
			TraceFindCriteriaResp traceResp = null;
			List<Address> addresses = null;
			
			LOG.debug("Get trace data");
			TraceFindCriteriaReq traceFindReq = new TraceFindCriteriaReq();
			traceFindReq.setCurrentPage(req.getTraceCurrentPage());
			traceFindReq.setItemsPerPage(req.getTraceItemsPerPage());
			traceFindReq.setProductId(req.getProductId());
			traceFindReq.setContractNo(String.valueOf(mainTask.get(prodSetting.getContractNoColumnName())));
			
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
			
			Object obj;
			String result = "", result2 = "";
			
			//--: Concat fields
			for(Entry<String, List<ColumnFormat>> entry : sameColumnAlias.entrySet()) {
				List<ColumnFormat> value = entry.getValue();
				if(value.size() < 2) continue;
				
				result = "";
				result2 = "";
				for (ColumnFormat col : value) {
					obj = mainTask.get(col.getColumnName());
					if(!(obj instanceof String)) break;
					result += " " + obj;
					result2 += "\n" + obj;
					mainTask.remove(col.getColumnName());
				}
				mainTask.put(value.get(0).getColumnName(), result.trim());
				mainTask.put(value.get(0).getColumnName() + "_hide", result2.trim());
			}
			//--: End Concat fields
			
			TaskDetailViewCriteriaResp resp = new TaskDetailViewCriteriaResp();
			resp.setTaskDetail(mainTask);
			resp.setColFormMap(map);
			resp.setGroupDatas(groupDatas);
			resp.setTraceResp(traceResp);
			resp.setAddresses(addresses);
			
			LOG.debug("Call getRelatedData");
			Map<String, RelatedData> relatedData = getRelatedData(template, addrReq.getContractNo(), addrReq.getIdCardNo());				
			resp.setRelatedData(relatedData);
						
			LOG.debug("Call getGetPayment");
			getPayment(template, 
					   prodSetting.getContractNoColumnNamePayment(), 
					   traceFindReq.getContractNo(), 
					   columnFormatsPayment, 
					   resp, req.getCurrentPagePayment(), req.getItemsPerPagePayment());
			
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
			Query query = Query.query(Criteria.where("_id").is(req.getId()));
			
			for (ColumnFormat colForm : columnFormats) {
				if(!colForm.getDetIsActive()) continue;
				
				query.fields().include(colForm.getColumnName());
			}
			
			LOG.debug("Get Task");
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Map mainTask = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
			List<Map<String, String>> owner = (List<Map<String, String>>)mainTask.get(SYS_OWNER.getName());
			String username = owner.get(0).get("username");
			
			LOG.debug("find user");
			Users user = userRepository.findByUsername(username);
			
			mainTask.put("owner_fullname", user.getFirstName() + " " + user.getLastName());
			mainTask.put("owner_tel", user.getPhoneNumber());
			
			TaskDetailViewCriteriaResp resp = new TaskDetailViewCriteriaResp();
			resp.setTaskDetail(mainTask);
			
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
				criteria = Criteria.where(SYS_FILE_ID.getName()).in(req.getTaskFileId()).and(SYS_OWNER.getName()).is(null);
				query = Query.query(criteria).with(new Sort(Sort.Direction.DESC, productSetting.getBalanceColumnName()));
				break;
			case TRANSFER:
				List<String> usernames = req.getTransferUsernames();
				criteria = Criteria.where(SYS_FILE_ID.getName()).in(req.getTaskFileId()).and(SYS_OWNER.getName() + ".0.username").in(usernames);
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
			LOG.debug("Num of " + SYS_OWNER.getName() + " to be assigned: " + userNum);
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
			List<Map<String, String>> owners;
			Double calColVal;
			
			for (Map map : taskDetails) {
				calColVal = (Double)map.get(productSetting.getBalanceColumnName());
				if(calColVal == null) continue;
				
				owners = (List<Map<String, String>>)map.get(SYS_OWNER.getName());
				if(owners == null) owners = new ArrayList<>();
				
				if(count == userNum) {
					if(method == AssignMethodConstant.RANDOM) {
						index = RandomUtil.random(userNum);						
					}
					count = 0;					
				}
				
				owners.add(0, req.getUsernames().get(index.get(count)));
				map.put(SYS_OWNER.getName(), owners);
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
			
			
			criteria = Criteria.where(SYS_FILE_ID.getName()).is(req.getTaskFileId()).and(SYS_OWNER.getName()).is(null).and(SYS_IS_ACTIVE.getName() + ".status").is(true);
			long noOwnerCount = template.count(Query.query(criteria), NEW_TASK_DETAIL.getName());
			LOG.debug("rowNum of don't have owner yet: " + noOwnerCount);
			
			resp.setNoOwnerCount(noOwnerCount);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void uploadAssing(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String productId, String taskFileId) throws Exception {
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
			
			if(productSetting == null || StringUtils.isBlank(productSetting.getContractNoColumnName())) {
				throw new Exception("ProductSetting is null");
			}
			
			String contractNoCol = productSetting.getContractNoColumnName();
			String user = "user";
			Sheet sheet = workbook.getSheetAt(0);
			AssignByLoad assignByLoad = new AssignByLoad();
			
			LOG.debug("Call getHeaderAssign");
			Map<String, Integer> headerIndex = assignByLoad.getHeaderAssign(sheet, contractNoCol, user);
			if(headerIndex.size() == 0) {
				throw new Exception("Not found header");
			}
			
			LOG.debug("Call getBodyAssign");
			Map<String, List<String>> assignVal = assignByLoad.getBodyAssign(sheet, headerIndex, contractNoCol, user);
			if(assignVal.size() == 0) {
				throw new Exception("Not found content");
			}
			
			LOG.debug("Find all Users");
			List<Users> users = templateCenter.find(Query.query(Criteria.where("username").in(assignVal.keySet())), Users.class);
			if(users.size() == 0) {
				throw new Exception("Not found users");
			}
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			LOG.debug("Call assign");
			assignByLoad.assign(users, assignVal, template, contractNoCol, taskFileId);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
		}
	}
	
	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats, boolean isAssign) {
		if(columnFormats == null) return null;
		
		List<ColumnFormat> result = new ArrayList<>();
		
		if(isAssign) {
			ColumnFormat isActive = new ColumnFormat(SYS_IS_ACTIVE.getName(), true);
			isActive.setColumnNameAlias("สถานะใช้งาน");
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
			
			criteria.and(SYS_OWNER.getName()).is(null).and(SYS_IS_ACTIVE.getName() + ".status").is(true);
			long noOwnerCount = template.count(Query.query(criteria), NEW_TASK_DETAIL.getName());
			LOG.debug("rowNum of don't have owner yet: " + noOwnerCount);
			LOG.debug("End");
			return noOwnerCount;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private Map<String, Long> countUserTask(MongoTemplate template, String taskFileId, UserByProductCriteriaResp userResp) {
		try {
			LOG.debug("Start");
			Map<String, Long> userTaskCount = new HashMap<>();
			Criteria criteria;
			
			for (Users u : userResp.getUsers()) {
				
				if(StringUtils.isBlank(taskFileId)) {
					criteria = new Criteria();
				} else {				
					criteria = Criteria.where(SYS_FILE_ID.getName()).is(taskFileId);
				}
				
				criteria
				.and(SYS_IS_ACTIVE.getName() + ".status").is(true)
				.and(SYS_OWNER.getName() + ".0.username").is(u.getUsername());
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
			List<ColumnFormat> importMenuColForm;
			List<GroupData> importMenuGroupDatas;
			List<Map> dataMap;
			Map<String, RelatedData> relatedData = new LinkedHashMap<>();
			Map<Integer, List<ColumnFormat>> othersMap;
			List<ColumnFormat> othersColFormLst;
			RelatedData data;
			ImportOthersSetting importOthersSetting;
			String childIdCardNoColumnName, childContractNoColumnName;
			List<Criteria> multiOr;
			Criteria criteria;
			Criteria[] multiOrArr;
			
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
					
					for (ColumnFormat colForm : importMenuColForm) {
						if(!colForm.getDetIsActive()) continue;
						
						relatedDataQuery.fields().include(colForm.getColumnName());
						
						if(othersMap.containsKey(colForm.getDetGroupId())) {					
							othersColFormLst = othersMap.get(colForm.getDetGroupId());
							othersColFormLst.add(colForm);
						} else {
							othersColFormLst = new ArrayList<>();
							othersColFormLst.add(colForm);
							othersMap.put(colForm.getDetGroupId(), othersColFormLst);
						}
					}
					
					dataMap = template.find(relatedDataQuery, Map.class, importMenu.getId());
					
					if(dataMap.size() == 0) dataMap.add(new HashMap<>());
					
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
			String conNoColPayment, 
			String contractNo, 
			List<ColumnFormat> columnFormatsPayment, 
			TaskDetailViewCriteriaResp resp,
			int currentPage, int itemsPerPage) {
		
		if(columnFormatsPayment == null || StringUtils.isBlank(conNoColPayment)) return ;
		
		Query paymentQuery = Query.query(Criteria.where(conNoColPayment).is(contractNo));
		
		for (ColumnFormat colForm : columnFormatsPayment) {
			if(!colForm.getIsActive()) continue;
			
			paymentQuery.fields().include(colForm.getColumnName());
		}
		
		long totalItems = template.count(paymentQuery, "paymentDetail");
		resp.setPaymentTotalItems(totalItems);
		
		paymentQuery.with(new PageRequest(currentPage - 1, itemsPerPage));
		paymentQuery.with(new Sort(Direction.DESC, SYS_CREATED_DATE_TIME.getName()));
		
		List<Map> paymentDetails = template.find(paymentQuery, Map.class, "paymentDetail");
		resp.setPaymentDetails(paymentDetails);
	}
	
}
