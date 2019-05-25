package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
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
import com.may.ple.backend.criteria.SmsCriteriaReq;
import com.may.ple.backend.criteria.SmsCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.MappingUtil;
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
				if(taskDetail.get(productSetting.getContractNoColumnName()) == null) {
					throw new Exception("Not found contract_no column");
				}
					
				update = new Update();
				update.set("taskDetail", taskDetail);
				update.set("status", 0);
				update.set("createdDateTime", now.getTime());
				update.set("createdBy", new ObjectId(user.getId()));
				update.set("createdByName", user.getShowname());
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

			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			List<ColumnFormat> headers = product.getColumnFormats();
			resp.setHeaders(getColumnFormatsActive(headers));
			
			String dateColumn;
			if(req.getStatus().intValue() == 0) {
				dateColumn = "createdDateTime";
			} else {
				dateColumn = "sentDateTime";
			}
			
			Criteria criteria = Criteria.where("status").is(req.getStatus());
			if(req.getDateFrom() != null) {
				if(req.getDateTo() != null) {
					criteria.and(dateColumn).gte(req.getDateFrom()).lte(req.getDateTo());			
				} else {
					criteria.and(dateColumn).gte(req.getDateFrom());
				}
			} else if(req.getDateTo() != null) {				
				criteria.and(dateColumn).lte(req.getDateTo());
			}
			
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
			
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			List<Map<String, String>> userList;
			for (Map<String, Object> sms : smses) {
				userList = MappingUtil.matchUserId(users, ((List)(((Map)sms.get("taskDetail")).get("sys_owner_id"))).get(0).toString());
				((Map)sms.get("taskDetail")).put("sys_owner", userList == null ? "" : userList.get(0).get("showname"));
			}
			
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
			List<ObjectId> ids = new ArrayList<>();
			
			for (String id : req.getIds()) {
				ids.add(new ObjectId(id));
			}
			
			template.remove(Query.query(Criteria.where("_id").in(ids)), "sms");			
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
