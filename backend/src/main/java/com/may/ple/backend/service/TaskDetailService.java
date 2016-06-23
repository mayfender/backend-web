package com.may.ple.backend.service;

import static com.may.ple.backend.constant.SysFieldConstant.OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_IS_ACTIVE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OLD_ORDER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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
import com.may.ple.backend.constant.AssignMethodConstant;
import com.may.ple.backend.constant.ColumnSearchConstant;
import com.may.ple.backend.constant.TaskTypeConstant;
import com.may.ple.backend.criteria.TaskDetailCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailCriteriaResp;
import com.may.ple.backend.criteria.UpdateTaskIsActiveCriteriaReq;
import com.may.ple.backend.criteria.UpdateTaskIsActiveCriteriaResp;
import com.may.ple.backend.criteria.UserByProductCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.IsActive;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.IsActiveModel;
import com.may.ple.backend.utils.RandomUtil;

@Service
public class TaskDetailService {
	private static final Logger LOG = Logger.getLogger(TaskDetailService.class.getName());
	private DbFactory dbFactory;
	private UserAction userAct;
	private MongoTemplate templateCenter;
	
	@Autowired
	public TaskDetailService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.userAct = userAct;
	}
	
	public TaskDetailCriteriaResp find(TaskDetailCriteriaReq req) throws Exception {
		try {
			TaskDetailCriteriaResp resp = new TaskDetailCriteriaResp();
			
			if(req.getColumnSearchSelected() == null) req.setColumnSearchSelected(1);
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			
			LOG.debug("Before size: " + columnFormats.size());
			columnFormats = getColumnFormatsActive(columnFormats);
			LOG.debug("After size: " + columnFormats.size());
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			//-------------------------------------------------------------------------------------
			Criteria criteria = Criteria.where("taskFileId").is(req.getTaskFileId());
			Query query = Query.query(criteria);
			
			if(req.getIsActive() != null) {
				criteria.and(SYS_IS_ACTIVE.getName() + ".status").is(req.getIsActive());
			}
			
			if(ColumnSearchConstant.OWNER == ColumnSearchConstant.findById(req.getColumnSearchSelected())) {
				if(StringUtils.isBlank(req.getKeyword())) {
					criteria.and(OWNER.getName()).is(null);
				} else {
					criteria.and(OWNER.getName() + ".0.username").regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE));					
				}
			}
			
			//-------------------------------------------------------------------------------------
			Field fields = query.fields();
			List<Criteria> multiOr = new ArrayList<>();
			
			for (ColumnFormat columnFormat : columnFormats) {
				fields.include(columnFormat.getColumnName());
				
				if(ColumnSearchConstant.Others == ColumnSearchConstant.findById(req.getColumnSearchSelected())) {
					if(columnFormat.getDataType() != null) {
						if(columnFormat.getDataType().equals("str")) {
							multiOr.add(Criteria.where(columnFormat.getColumnName()).regex(Pattern.compile(req.getKeyword() == null ? "" : req.getKeyword(), Pattern.CASE_INSENSITIVE)));
						} else if(columnFormat.getDataType().equals(OWNER.getName())) {
							multiOr.add(Criteria.where(columnFormat.getColumnName() + ".0.username").regex(Pattern.compile(req.getKeyword() == null ? "" : req.getKeyword(), Pattern.CASE_INSENSITIVE)));
						} else if(columnFormat.getDataType().equals("num")) {
							//--: Ignore right now.
						}
					} else {
						LOG.debug(columnFormat.getColumnName() + "' dataType is null");
					}
				}
			}
			
			Criteria[] multiOrArr = multiOr.toArray(new Criteria[multiOr.size()]);
			if(multiOrArr.length > 0) {
				criteria.orOperator(multiOrArr);				
			}
			
			//-------------------------------------------------------------------------------------
			long totalItems = template.count(query, "newTaskDetail");
			
			//-------------------------------------------------------------------------------------
			query = query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			
			if(req.getColumnName() == null) {
				query.with(new Sort(SYS_OLD_ORDER.getName()));
			} else {				
				query.with(new Sort(Direction.fromString(req.getOrder()), req.getColumnName()));
			}
			
			List<Map> taskDetails = template.find(query, Map.class, "newTaskDetail");			
			
			//-------------------------------------------------------------------------------------
			for (Map map : taskDetails) {
				map.put("id", map.get("_id").toString()); 
				map.remove("_id");
			}
			
			//-------------------------------------------------------------------------------------
			criteria = Criteria.where("taskFileId").is(req.getTaskFileId()).and(OWNER.getName()).is(null).and(SYS_IS_ACTIVE.getName() + ".status").is(true);
			long noOwnerCount = template.count(Query.query(criteria), "newTaskDetail");
			LOG.debug("rowNum of don't have owner yet: " + noOwnerCount);
			//-------------------------------------------------------------------------------------
			
			UserByProductCriteriaResp userResp = userAct.getUserByProductToAssign(req.getProductId());
			Map<String, Long> userTaskCount = new HashMap<>();
			
			for (Users u : userResp.getUsers()) {
				criteria = Criteria.where(SYS_IS_ACTIVE.getName() + ".status").is(true).and("taskFileId").is(req.getTaskFileId())
				.and(OWNER.getName() + ".0.username").is(u.getUsername());
				userTaskCount.put(u.getUsername(), template.count(Query.query(criteria), "newTaskDetail"));
			}
			//-------------------------------------------------------------------------------------
			
			resp.setUserTaskCount(userTaskCount);
			resp.setUsers(userResp.getUsers());
			resp.setHeaders(columnFormats);
			resp.setTotalItems(totalItems);
			resp.setTaskDetails(taskDetails);
			resp.setNoOwnerCount(noOwnerCount);
			
			if(product.getProductSetting() != null) {		
				resp.setBalanceColumn(product.getProductSetting().getBalanceColumn());
			}
			
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
			
			Criteria criteria = Criteria.where("_id").in(req.getTaskIds());
			Query query = Query.query(criteria).with(new Sort(Sort.Direction.DESC, req.getCalColumn()));
			
			List<Map> taskDetails = template.find(query, Map.class, "newTaskDetail");
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
			
			TaskTypeConstant taskType = TaskTypeConstant.findById(req.getTaskType());
			LOG.debug(taskType);
			Query query;
			Criteria criteria;
			
			switch (taskType) {
			case EMPTY:
				criteria = Criteria.where("taskFileId").in(req.getTaskFileId()).and(OWNER.getName()).is(null);
				query = Query.query(criteria).with(new Sort(Sort.Direction.DESC, req.getCalColumn()));
				break;
			case TRANSFER:
				List<String> usernames = req.getTransferUsernames();
				criteria = Criteria.where("taskFileId").in(req.getTaskFileId()).and(OWNER.getName() + ".0.username").in(usernames);
				query = Query.query(criteria).with(new Sort(Sort.Direction.DESC, req.getCalColumn()));
				break;

			default: throw new Exception("TaskType not found.");
			}
			
			List<Map> taskDetails = template.find(query, Map.class, "newTaskDetail");
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
			LOG.debug("Num of " + OWNER.getName() + " to be assigned: " + userNum);
			int count = 0;
			
			AssignMethodConstant method = AssignMethodConstant.findById(req.getMethodId());
			LOG.debug(method);
			List<Integer> index;
			
			if(method == AssignMethodConstant.RANDOM) {
				index = RandomUtil.random(userNum);				
			} else {
				index = RandomUtil.order(userNum);
			}
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			List<Map<String, String>> owners;
			Double calColVal;
			
			for (Map map : taskDetails) {
				calColVal = (Double)map.get(req.getCalColumn());
				if(calColVal == null) continue;
				
				owners = (List<Map<String, String>>)map.get(OWNER.getName());
				if(owners == null) owners = new ArrayList<>();
				
				if(count == userNum) {
					if(method == AssignMethodConstant.RANDOM) {
						index = RandomUtil.random(userNum);						
					}
					count = 0;					
				}
				
				owners.add(0, req.getUsernames().get(index.get(count)));
				map.put(OWNER.getName(), owners);
				template.save(map, "newTaskDetail");
				
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
				template.updateFirst(Query.query(criteria), Update.update(SYS_IS_ACTIVE.getName(), new IsActive(isActive.getStatus(), "")), "newTaskDetail");						
			}
			
			
			criteria = Criteria.where("taskFileId").is(req.getTaskFileId()).and(OWNER.getName()).is(null).and(SYS_IS_ACTIVE.getName() + ".status").is(true);
			long noOwnerCount = template.count(Query.query(criteria), "newTaskDetail");
			LOG.debug("rowNum of don't have owner yet: " + noOwnerCount);
			
			resp.setNoOwnerCount(noOwnerCount);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats) {
		ColumnFormat isActive = new ColumnFormat(SYS_IS_ACTIVE.getName(), true);
		isActive.setColumnNameAlias("สถานะใช้งาน");
		isActive.setDataType(SYS_IS_ACTIVE.getName());
		
		List<ColumnFormat> result = new ArrayList<>();
		result.add(isActive);
		
		for (ColumnFormat colFormat : columnFormats) {
			if(colFormat.getIsActive()) {
				result.add(colFormat);
			}
		}
		
		return result;
	}
	
}
