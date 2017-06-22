package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.constant.SysFieldConstant;
import com.may.ple.backend.criteria.FindToPrintCriteriaReq;
import com.may.ple.backend.criteria.FindToPrintCriteriaResp;
import com.may.ple.backend.criteria.SaveToPrintCriteriaReq;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.NoticeToPrint;
import com.may.ple.backend.entity.NoticeXDocFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class NoticeManagerService {
	private static final Logger LOG = Logger.getLogger(NoticeManagerService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	private UserAction userAct;
	
	@Autowired
	public NoticeManagerService(DbFactory dbFactory, MongoTemplate templateCore, UserAction userAct) {
		this.dbFactory = dbFactory;
		this.templateCore = templateCore;
		this.userAct = userAct;
	}
	
	public void saveToPrint(SaveToPrintCriteriaReq req) throws Exception {		
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Query query = Query.query(Criteria.where("id").is(req.getNoticeId()));
			query.fields().include("templateName");
					
			Log.debug("Get NoticeXDocFile");
			NoticeXDocFile file = template.findOne(query, NoticeXDocFile.class);
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			
			Map<String, Object> noticeToPrint = new HashMap<>();
			noticeToPrint.put("address", req.getAddress());
			noticeToPrint.put("customerName", req.getCustomerName());
			noticeToPrint.put("dateInput", req.getDateInput());
			noticeToPrint.put("noticeId", new ObjectId(req.getNoticeId()));
			noticeToPrint.put("noticeName", file.getTemplateName());
			noticeToPrint.put("taskDetailId", new ObjectId(req.getTaskDetailId()));
			noticeToPrint.put("createdDateTime", new Date());
			noticeToPrint.put("createdBy", new ObjectId(user.getId()));
			noticeToPrint.put("createdByName", user.getShowname());
			
			Log.debug("Get Product");
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			List<ColumnFormat> headers = product.getColumnFormats();
			headers = getColumnFormatsActive(headers);
			
			query = Query.query(Criteria.where("_id").is(req.getTaskDetailId()));
			Field fields = query.fields().include(SYS_OWNER_ID.getName()).exclude("_id");
			
			boolean isExis = template.collectionExists(NoticeToPrint.class);
			if(!isExis) {
				template.createCollection(NoticeToPrint.class);
			}
			
			DBCollection collection = template.getCollection("noticeToPrint");
			collection.createIndex(new BasicDBObject("address", 1));
			collection.createIndex(new BasicDBObject("noticeName", 1));
			collection.createIndex(new BasicDBObject("createdDateTime", 1));
			collection.createIndex(new BasicDBObject("createdBy", 1));
			
			for (ColumnFormat colForm : headers) {
				fields.include(colForm.getColumnName());
				collection.createIndex(new BasicDBObject(colForm.getColumnName(), 1));
			}
			
			LOG.debug("Find taskDetail");
			Map taskDetail = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
			
			LOG.debug("Find users");
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			List<String> ownerId = (List)taskDetail.get(SYS_OWNER_ID.getName());
			List<Map<String, String>> userList = MappingUtil.matchUserId(users, ownerId.get(0));
			Map u = (Map)userList.get(0);
			taskDetail.put(SYS_OWNER.getName(), u.get("showname"));
			
			taskDetail.putAll(noticeToPrint);
			
			Log.debug("Save noticeToPrint");
			template.save(taskDetail, "noticeToPrint");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Map findToPrintById(String productId, String id) throws Exception {		
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			Map noticeToPrint = template.findOne(Query.query(Criteria.where("_id").is(id)), Map.class, "noticeToPrint");	
			return noticeToPrint;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public FindToPrintCriteriaResp findToPrint(FindToPrintCriteriaReq req) throws Exception {		
		try {
			FindToPrintCriteriaResp resp = new FindToPrintCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Log.debug("Get Product");
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			List<ColumnFormat> headers = product.getColumnFormats();
			List<Criteria> multiOrTaskDetail = new ArrayList<>();
			headers = getColumnFormatsActive(headers);
			resp.setHeaders(headers);
			
			for (ColumnFormat columnFormat : headers) {
				if(columnFormat.getColumnName().equals(SysFieldConstant.SYS_OWNER.getName())) continue;
				
				if(!StringUtils.isBlank(req.getKeyword())) {
					if(columnFormat.getDataType() != null) {
						if(columnFormat.getDataType().equals("str")) {
							multiOrTaskDetail.add(Criteria.where(columnFormat.getColumnName()).regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));							
						} else if(columnFormat.getDataType().equals("num")) {
							//--: Ignore right now.
						}
					} else {
						LOG.debug(columnFormat.getColumnName() + "' dataType is null");
					}
				}
			}
			
			if(!StringUtils.isBlank(req.getKeyword())) {
				multiOrTaskDetail.add(Criteria.where("address").regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));
				multiOrTaskDetail.add(Criteria.where("noticeName").regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));
				multiOrTaskDetail.add(Criteria.where("createdByName").regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));
			}
			
			Criteria criteria = new Criteria();
			
			if(!StringUtils.isBlank(req.getOwner())) {
				criteria.and(SYS_OWNER_ID.getName() + ".0").is(req.getOwner());										
			}
			
			if(req.getDateFrom() != null) {
				if(req.getDateTo() != null) {
					criteria.and("createdDateTime").gte(req.getDateFrom()).lte(req.getDateTo());			
				} else {
					criteria.and("createdDateTime").gte(req.getDateFrom());
				}
			} else if(req.getDateTo() != null) {				
				criteria.and("createdDateTime").lte(req.getDateTo());
			}
			
			Criteria[] multiOrArr = multiOrTaskDetail.toArray(new Criteria[multiOrTaskDetail.size()]);
			if(multiOrArr.length > 0) {
				criteria.orOperator(multiOrArr);				
			}
			
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			resp.setUsers(users);
			
			long totalItems = template.count(Query.query(criteria), NoticeToPrint.class);
			resp.setTotalItems(totalItems);
			if(totalItems == 0) {
				return resp;				
			}
			
			Query query = Query.query(criteria)
			.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			.with(new Sort(Sort.Direction.fromString(req.getOrder()), req.getColumnName()));
			
			List<Map> noticeToPrints = template.find(query, Map.class, "noticeToPrint");	
			resp.setNoticeToPrints(noticeToPrints);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteToPrint(FindToPrintCriteriaReq req) throws Exception {		
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			template.remove(Query.query(Criteria.where("id").is(req.getId())), NoticeToPrint.class);
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
