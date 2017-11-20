package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_PROBATION_OWNER_ID;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.constant.FileTypeConstant;
import com.may.ple.backend.constant.SysFieldConstant;
import com.may.ple.backend.criteria.ChangeStatusNoticeCriteriaReq;
import com.may.ple.backend.criteria.FindToPrintCriteriaReq;
import com.may.ple.backend.criteria.FindToPrintCriteriaResp;
import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.SaveToPrintCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.NoticeToPrint;
import com.may.ple.backend.entity.NoticeXDocFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.may.ple.backend.utils.PdfUtil;
import com.may.ple.backend.utils.XDocUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class NoticeManagerService {
	private static final Logger LOG = Logger.getLogger(NoticeManagerService.class.getName());
	private NoticeXDocUploadService xdocUploadService;
	private TaskDetailService taskDetailService;
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	private UserAction userAct;
	private UserService userService;
	@Value("${file.path.temp}")
	private String filePathTemp;
	
	@Autowired
	public NoticeManagerService(DbFactory dbFactory, MongoTemplate templateCore, UserAction userAct, 
			TaskDetailService taskDetailService, NoticeXDocUploadService xdocUploadService, UserService userService) {
		this.taskDetailService = taskDetailService;
		this.xdocUploadService = xdocUploadService;
		this.templateCore = templateCore;
		this.dbFactory = dbFactory;
		this.userAct = userAct;
		this.userService = userService;
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
			Boolean probation = user.getProbation();
			
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
			ColumnFormat columnFormatProbation = new ColumnFormat();
			columnFormatProbation.setColumnName(SYS_PROBATION_OWNER_ID.getName());
			headers.add(columnFormatProbation);
			
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
			
			if(probation != null && probation) {
				noticeToPrint.put("createdBy", ownerId.get(0));
				noticeToPrint.put("createdByName", u.get("showname"));
			}
			
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
	
	public String printBatchNotice(FindToPrintCriteriaReq req) throws Exception {		
		Date now = new Date();
		String fileNameGen = req.getProductId() + "_" + String.format("%1$tH%1$tM%1$tS%1$tL", now);
		
		try {
			LOG.debug("call findToPrint");
			FindToPrintCriteriaResp findToPrint = findToPrint(req, false);
			List<Map> noticeToPrints = findToPrint.getNoticeToPrints();
			List<String> ids = new ArrayList<>();
			
			LOG.debug("call getUserByProductToAssign");
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			
			for (Map noticeToPrint : noticeToPrints) {
				ids.add(noticeToPrint.get("taskDetailId").toString());
			}
			
			LOG.debug("call getTaskDetailToNotice");
			TaskDetailViewCriteriaReq taskReq = new TaskDetailViewCriteriaReq();
			taskReq.setIds(ids);
			taskReq.setProductId(req.getProductId());			
			TaskDetailViewCriteriaResp taskResp = taskDetailService.getTaskDetailToNotice(taskReq);
			
			List<Map> taskDetails = taskResp.getTaskDetails();
			List<String> odtFiles = new ArrayList<>();
			List<String> pdfFiles = new ArrayList<>();
			String mergeFileStr = "", pdfFileStr = "";
			List<Map<String, String>> userList;
			String generatedFilePath;
			List<String> ownerId;
			String filePath;
			Map userMap;
			byte[] data;
			int r = 0;
			
			for (Map noticeToPrint : noticeToPrints) {
				r++;
				
				for (Map taskDetail : taskDetails) {
					if(!taskDetail.get("_id").equals(noticeToPrint.get("taskDetailId"))) continue;
						
					ownerId = (List)taskDetail.get(SYS_OWNER_ID.getName());
					userList = MappingUtil.matchUserId(users, ownerId.get(0));
					
					if(userList != null && userList.size() > 0) {
						userMap = (Map)userList.get(0);
						taskDetail.put("owner_fullname", (userMap.get("firstName") == null ? "" : userMap.get("firstName")) + " " + (userMap.get("lastName") == null ? "" : userMap.get("lastName")));
						taskDetail.put("owner_tel", userMap.get("phone") == null ? "" : userMap.get("phone"));
					}
					noticeToPrint.put("address_sys", noticeToPrint.get("address"));
					noticeToPrint.put("today_sys", now);
					noticeToPrint.remove("address");
					
					if(noticeToPrint.get("customerName") != null) {
						if(!StringUtils.isBlank(noticeToPrint.get("customerName").toString())) {							
							noticeToPrint.put("customer_name_sys", noticeToPrint.get("customerName"));
						}
					}
					
					noticeToPrint.putAll(taskDetail);
					break;
				}
				
				LOG.debug("Get file");
				NoticeFindCriteriaReq reqNoticeFile = new NoticeFindCriteriaReq();
				reqNoticeFile.setProductId(req.getProductId());
				reqNoticeFile.setTemplateName(noticeToPrint.get("noticeName").toString());
				
				Map<String, String> map = xdocUploadService.getNoticeFile(reqNoticeFile);
				filePath = map.get("filePath");
				
				LOG.debug("call XDocUtil.generate");
				data = XDocUtil.generate(filePath, noticeToPrint);
				
				LOG.debug("Call saveToFile");
				generatedFilePath = xdocUploadService.saveToFile(filePathTemp, fileNameGen + "_" + r, FilenameUtils.getExtension(filePath), data);
				odtFiles.add(generatedFilePath);
				
				if((r % 100) == 0) {
					LOG.debug("r = " + r + " so start to merge odt and convert to pdf");
					mergeFileStr = filePathTemp + "/" + fileNameGen + "_merged_" + r + "." + FileTypeConstant.ODT.getName();
					pdfFileStr = xdocUploadService.createPdf(mergeFileStr, odtFiles);
					pdfFiles.add(pdfFileStr);
					odtFiles.clear();
					LOG.debug("Convert to pdf finished");
				}
			}
			
			if(odtFiles != null && odtFiles.size() > 0) {
				//--: Found the rest odt file so start to merge odt and convert to pdf again
				LOG.debug("Start merge odt and convert to pdf");
				mergeFileStr = filePathTemp + "/" + fileNameGen + "_merged_" + r + "." + FileTypeConstant.ODT.getName();
				pdfFileStr = xdocUploadService.createPdf(mergeFileStr, odtFiles);
				pdfFiles.add(pdfFileStr);
				LOG.debug("Convert to pdf finished");
				
				if(pdfFiles.size() == 1) {
					mergeFileStr = pdfFiles.get(0);					
				} else {					
					mergeFileStr = filePathTemp + "/" + fileNameGen + "_merged." + FileTypeConstant.PDF.getName();
					PdfUtil.mergePdf(pdfFiles, mergeFileStr);
				}
			} else if(pdfFiles != null && pdfFiles.size() > 0) {
				if(pdfFiles.size() == 1) {
					mergeFileStr = pdfFiles.get(0);					
				} else {					
					mergeFileStr = filePathTemp + "/" + fileNameGen + "_merged." + FileTypeConstant.PDF.getName();
					PdfUtil.mergePdf(pdfFiles, mergeFileStr);
				}
			} else {
				LOG.warn("Not found file to gen Notice");
			}
			
			LOG.info("End");
			return FilenameUtils.getName(mergeFileStr);
		} catch (Exception e) {
			LOG.error(e.toString());
			xdocUploadService.removeTrashFile(filePathTemp, fileNameGen);
			throw e;
		}
	}
	
	public FindToPrintCriteriaResp findToPrint(FindToPrintCriteriaReq req, boolean isPagging) throws Exception {		
		try {
			FindToPrintCriteriaResp resp = new FindToPrintCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Log.debug("Get Product");
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			List<ColumnFormat> headers = product.getColumnFormats();
			ProductSetting prodSetting = product.getProductSetting();
			List<Criteria> multiOrTaskDetail = new ArrayList<>();
			headers = getColumnFormatsActive(headers);
			resp.setHeaders(headers);
			List<String> probationUserIds = new ArrayList<>();
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			resp.setUsers(users);
			
			for (Users u : users) {
				if(u.getProbation() == null || !u.getProbation()) continue;
				probationUserIds.add(u.getId());
			}
			
			if(req.getIsInit() == null ? false : req.getIsInit()) {
				resp.setIsDisableNoticePrint(prodSetting.getIsDisableNoticePrint());
			}
			
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
			
			if(req.getStatus() != null) {
				if(req.getStatus()) {					
					criteria.and("printStatus").is(req.getStatus());										
				} else {
					criteria.and("printStatus").ne(true);
				}
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
			
			if(isPagging) {
				long totalItems = template.count(Query.query(criteria), NoticeToPrint.class);
				resp.setTotalItems(totalItems);
				if(totalItems == 0) {
					return resp;				
				}
			}
			
			Query query = Query.query(criteria)
			.with(new Sort(Sort.Direction.fromString(req.getOrder()), req.getColumnName()));
			
			if(isPagging) {
				query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			}
			
			List<Map> noticeToPrints = template.find(query, Map.class, "noticeToPrint");	
			resp.setNoticeToPrints(noticeToPrints);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void changeStatus(ChangeStatusNoticeCriteriaReq req) throws Exception {		
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			template.updateMulti(Query.query(Criteria.where("_id").in(req.getIds())), Update.update("printStatus", req.getStatus()), "noticeToPrint");
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
