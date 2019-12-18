package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_PAYMENT_DETAIL;
import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_PROBATION_OWNER_ID;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.ibm.icu.util.Calendar;
import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.constant.FileTypeConstant;
import com.may.ple.backend.constant.SysFieldConstant;
import com.may.ple.backend.constant.TPLTypeConstant;
import com.may.ple.backend.criteria.EngTplCriteriaReq;
import com.may.ple.backend.criteria.PaymentDetailCriteriaReq;
import com.may.ple.backend.criteria.PaymentDetailCriteriaResp;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.MappingUtil;
import com.may.ple.backend.utils.PdfUtil;
import com.may.ple.backend.utils.XDocUtil;
import com.mongodb.BasicDBObject;

@Service
public class PaymentDetailService {
	private static final Logger LOG = Logger.getLogger(PaymentDetailService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	private UserAction userAct;
	private UserService userService;
	private DymSearchService dymSearchService;
	private EngTplService engService;
	private NoticeXDocUploadService xdocUploadService;
	@Value("${file.path.temp}")
	private String filePathTemp;
	
	@Autowired
	public PaymentDetailService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct, 
								UserService userService, DymSearchService dymSearchService,
								EngTplService engService, NoticeXDocUploadService xdocUploadService) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.userAct = userAct;
		this.userService = userService;
		this.dymSearchService = dymSearchService;
		this.engService = engService;
		this.xdocUploadService = xdocUploadService;
	}
	
	public PaymentDetailCriteriaResp find(PaymentDetailCriteriaReq req, boolean isReport, List<String> includeFields, Sort sort) throws Exception {
		try {
			LOG.debug("Start find");
			PaymentDetailCriteriaResp resp = new PaymentDetailCriteriaResp();
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			List<ColumnFormat> columnFormatsPayment = product.getColumnFormatsPayment();
			List<ColumnFormat> taskDetailHeaders = product.getColumnFormats();
			ProductSetting productSetting = product.getProductSetting();
			String contractNoColumn = productSetting.getContractNoColumnNamePayment();				
			String sortingColPayment = productSetting.getSortingColumnNamePayment();
			String paydateColName = productSetting.getPaidDateColumnNamePayment();
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			taskDetailHeaders = getColumnFormatsActive(taskDetailHeaders);
			
			resp.setUsers(users);
			resp.setIsReceipt(productSetting.getReceipt() == null ? null : (Boolean)productSetting.getReceipt().get("isReceipt"));
			
			List<Integer> statuses = new ArrayList<>();
			statuses.add(1);
			resp.setDymSearch(dymSearchService.getFields(req.getProductId(), statuses));
			
			List<String> probationUserIds = new ArrayList<>();
			for (Users u : users) {
				if(u.getProbation() == null || !u.getProbation()) continue;
				probationUserIds.add(u.getId());
			}
			
			if(columnFormatsPayment == null) return resp;
			
			LOG.debug("Before size: " + columnFormatsPayment.size());
			columnFormatsPayment = getColumnFormatsActive(columnFormatsPayment);
			LOG.debug("After size: " + columnFormatsPayment.size());
			
			//-------------------------------------------------------------------------------------
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Criteria criteria = new Criteria();
			
			if(!StringUtils.isBlank(req.getFileId())) {				
				criteria.and(SYS_FILE_ID.getName()).is(req.getFileId());
			}
			if(!StringUtils.isBlank(req.getContractNo())) {
				criteria.and(contractNoColumn).is(req.getContractNo());
			}
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
			
			if(!StringUtils.isBlank(req.getDymSearchFiedVal())) {
				criteria.and("taskDetail." + req.getDymSearchFiedName()).is(req.getDymSearchFiedVal());
			}
			
			//-------------------------------------------------------------------------------------
			Query query = Query.query(criteria);
			Field fields = query.fields();
			fields.include(SYS_CREATED_DATE_TIME.getName());
			fields.include("sys_printedDateTime");
			fields.include("sys_receiptNo");
			fields.include(SYS_OWNER_ID.getName());
			fields.include("taskDetail._id");
			fields.include("taskDetail." + SYS_OWNER.getName());
			List<Criteria> multiOr = new ArrayList<>();
			
			if(includeFields != null) {
				for (String field : includeFields) {
					fields.include(field);
				}
			}
			
			if(productSetting.getPocModule() != null && productSetting.getPocModule().equals(1)) {
				ColumnFormat ext = new ColumnFormat();
				ext.setColumnName("loan_type_pay");
				ext.setColumnNameAlias("จ่ายที่สัญญา");
				ext.setDataType("str");
				columnFormatsPayment.add(ext);
			}
			
			for (ColumnFormat columnFormat : columnFormatsPayment) {
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
			
			for (ColumnFormat columnFormat : taskDetailHeaders) {
				if(columnFormat.getColumnName().equals(SysFieldConstant.SYS_OWNER.getName())) continue;
				
				fields.include("taskDetail." + columnFormat.getColumnName());
				
				if(!StringUtils.isBlank(req.getKeyword())) {
					if(columnFormat.getDataType() != null) {
						if(columnFormat.getDataType().equals("str")) {
							multiOr.add(Criteria.where("taskDetail." + columnFormat.getColumnName()).regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));							
						} else if(columnFormat.getDataType().equals("num")) {
							//--: Ignore right now.
						}
					} else {
						LOG.debug(columnFormat.getColumnName() + "' dataType is null");
					}
				}
			}
			
			if(req.getDateFrom() != null) {
				if(req.getDateTo() != null) {
					criteria.and(paydateColName).gte(req.getDateFrom()).lte(req.getDateTo());			
				} else {
					criteria.and(paydateColName).gte(req.getDateFrom());
				}
			} else if(req.getDateTo() != null) {				
				criteria.and(paydateColName).lte(req.getDateTo());
			}
			
			Criteria[] multiOrArr = multiOr.toArray(new Criteria[multiOr.size()]);
			if(multiOrArr.length > 0) {
				criteria.orOperator(multiOrArr);				
			}
			
			//-------------------------------------------------------------------------------------
			long totalItems = 0;
			if(!isReport) {
				LOG.debug("Start Count paymentDetail record");
				totalItems = template.count(query, NEW_PAYMENT_DETAIL.getName());
				LOG.debug("End Count paymentDetail record");
			}
			
			//-------------------------------------------------------------------------------------
			if(req.getCurrentPage() != null) {
				query = query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			}
			
			if(sort == null) {				
				query.with(new Sort(Direction.DESC, StringUtils.isBlank(sortingColPayment) ? SYS_CREATED_DATE_TIME.getName() : sortingColPayment));
			} else {
				query.with(sort);
			}
			
			LOG.debug("Start find paymentDetail");
			List<Map> paymentDetails = template.find(query, Map.class, NEW_PAYMENT_DETAIL.getName());			
			LOG.debug("End find paymentDetail");
			
			resp.setHeaders(columnFormatsPayment);
			resp.setTaskDetailHeaders(taskDetailHeaders);
			resp.setTotalItems(totalItems);
			resp.setPaymentDetails(paymentDetails);
			
			LOG.debug("End find");
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Map printReceipt(PaymentDetailCriteriaReq req) throws Exception {
		try {
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			
			BasicDBObject fields = new BasicDBObject();
			fields.append(productSetting.getContractNoColumnNamePayment(), 1);
			fields.append("paid_amount", 1);
			fields.append("sys_printedDateTime", 1);
			fields.append("sys_receiptNo", 1);
			fields.append("sys_countDup", 1);
			fields.append("taskDetailFull." + SYS_OWNER_ID.getName(), 1);
			for (ColumnFormat colForm : columnFormats) {
				if(!colForm.getDetIsActive()) continue;
				fields.append("taskDetailFull." + colForm.getColumnName(), 1);
			}
			
			List<ObjectId> ids = new ArrayList<>();
			
			// Find all with search criteria.
			if(req.getIsAllOfAllSelected() != null && req.getIsAllOfAllSelected()) {
				req.setCurrentPage(null);
				PaymentDetailCriteriaResp resp = find(req, true, null, null);
				List<Map> paymentDetails = resp.getPaymentDetails();
				for (Map map : paymentDetails) {
					ids.add(new ObjectId(String.valueOf(map.get("_id"))));
				}
			} else {
				for (String id : req.getIds()) {
					ids.add(new ObjectId(id));
				}
			}
			
			BasicDBObject project = new BasicDBObject("$project", fields);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("sys_createdDateTime", -1));
			Criteria criteria = Criteria.where("_id").in(ids);
			MatchOperation match = Aggregation.match(criteria);
			
			Aggregation agg = Aggregation.newAggregation(
					match,
					new CustomAggregationOperation(sort),
					new CustomAggregationOperation(
					        new BasicDBObject(
						            "$lookup",
						            new BasicDBObject("from", NEW_TASK_DETAIL.getName())
						                .append("localField", productSetting.getContractNoColumnNamePayment())
						                .append("foreignField", productSetting.getContractNoColumnName())
						                .append("as", "taskDetailFull")
					        	)),
					Aggregation.unwind("taskDetailFull"),
					new CustomAggregationOperation(project)
			);
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			AggregationResults<Map> aggregate = template.aggregate(agg, NEW_PAYMENT_DETAIL.getName(), Map.class);
			
			Date date = Calendar.getInstance().getTime();
			String format = productSetting.getReceipt().get("format").toString();
			String[] formatArr = format.split("=");
			String type = formatArr[0];
			String columnArr[] = formatArr[1].split(",");
			List<Map> payments = aggregate.getMappedResults();
			Map<String, Long> chkDup = new HashMap<>();
			Criteria criteria2;
			String genRcNo;
			long countDup;
			
			for (Map map : payments) {
				if(map.get("sys_receiptNo") != null) continue;
				
				if(type.equals("1")) {	
					if(chkDup.containsKey(map.get(productSetting.getContractNoColumnNamePayment()))) {
						countDup = chkDup.get(map.get(productSetting.getContractNoColumnNamePayment())) + 1;
						LOG.info("Same contract no.");
					} else {
						if(map.get("sys_printedDateTime") != null) continue;
						
						criteria2 = Criteria.where(productSetting.getContractNoColumnNamePayment()).is(map.get(productSetting.getContractNoColumnNamePayment()));
						criteria2.and("sys_printedDateTime").ne(null);							
						countDup = template.count(Query.query(criteria2), NEW_PAYMENT_DETAIL.getName()) + 1;
					}
					chkDup.put(map.get(productSetting.getContractNoColumnNamePayment()).toString(), countDup);
					
					genRcNo = genRC_1(map, columnArr, date, countDup);
					map.put("sys_receiptNo", genRcNo);
					map.put("sys_countDup", countDup);
				}
			}
			
			LOG.debug("Get file");			
			EngTplCriteriaReq engReq = new EngTplCriteriaReq();
			engReq.setProductId(req.getProductId());
			engReq.setType(TPLTypeConstant.RECEIPT.getId());
			
			String resultFile = pdfGen(productSetting, req.getProductId(), engService.getFile(engReq), payments);
			
			//--- Update print receipt date time.
			List<BasicDBObject> list = new ArrayList<>();
			list.add(new BasicDBObject("printedDateTime", date));
			//--
			BasicDBObject obj = new BasicDBObject();
			obj.append("$each", list);
			obj.append("$sort", new BasicDBObject("printedDateTime", -1));
			
			Map printedResult = new HashMap();
			Map printedObj;
			Criteria updateCtr;
			Update update;
			for (Map map : payments) {
				update = new Update();
				printedObj = new HashMap();
				if(map.get("sys_printedDateTime") == null) {
					update.set("sys_printedDateTime", date);
					update.set("sys_receiptNo", map.get("sys_receiptNo"));
					update.set("sys_countDup", map.get("sys_countDup"));
					
					printedObj.put("printedDateTime", date);
					printedObj.put("sys_receiptNo", map.get("sys_receiptNo"));
					printedResult.put(map.get("_id"), printedObj);
				} else {
					printedObj.put("printedDateTime", map.get("sys_printedDateTime"));
					printedObj.put("sys_receiptNo", map.get("sys_receiptNo"));
					printedResult.put(map.get("_id"), printedObj);
				}
				update.push("sys_printedDateTimes", obj);
				updateCtr = Criteria.where("_id").is(map.get("_id"));
				template.updateFirst(Query.query(updateCtr), update, NEW_PAYMENT_DETAIL.getName());
			}
			
			Map result = new HashMap();
			result.put("resultFile", resultFile);
			result.put("printedResult", printedResult);
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			LOG.info("End");			
		}
	}
	
	//---------------------------------------: Private Zone :-----------------------------------------------
	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats) {
		List<ColumnFormat> result = new ArrayList<>();
		
		for (ColumnFormat colFormat : columnFormats) {
			if(colFormat.getIsActive()) {
				result.add(colFormat);
			}
		}
		
		return result;
	}
	
	private String pdfGen(ProductSetting productSetting, String productId, String templatePath, List<Map> payments) throws Exception {
		Date now = Calendar.getInstance().getTime();
		String fileNameGen = productId + "_" + String.format("%1$td%1$tm%1$tY_%1$tH%1$tM%1$tS%1$tL", now);
		
		try {
			String host = productSetting.getOpenOfficeHost();
			Integer port = productSetting.getOpenOfficePort();
			List<Users> users = userAct.getUserByProductToAssign(productId).getUsers();
			List<String> odtFiles = new ArrayList<>();
			List<String> pdfFiles = new ArrayList<>();
			String mergeFileStr = "", pdfFileStr = "";
			List<Map<String, String>> userList;
			String generatedFilePath;
			Map taskDetailFull;
			List<String> owner;
			String userId;
			byte[] data;
			int r = 0;
			
			for (Map map : payments) {
				r++;
				map.put("today_sys", now);
				taskDetailFull = (Map)map.get("taskDetailFull");
				owner = (List)taskDetailFull.get(SYS_OWNER_ID.getName());
				
				if(owner != null) {
					userId = owner.get(0);
					userList = MappingUtil.matchUserId(users, userId);
					map.put("owner_fullname", StringUtils.trimToEmpty(userList.get(0).get("firstName")) + " " + StringUtils.trimToEmpty(userList.get(0).get("lastName")));
				}
				
				LOG.debug("call XDocUtil.generate");
				data = XDocUtil.generate(templatePath, map);
				
				LOG.debug("Call saveToFile");
				generatedFilePath = xdocUploadService.saveToFile(filePathTemp, fileNameGen + "_" + r, FilenameUtils.getExtension(templatePath), data);
				odtFiles.add(generatedFilePath);
				
				if((r % 100) == 0) {
					LOG.debug("r = " + r + " so start to merge odt and convert to pdf");
					mergeFileStr = filePathTemp + "/" + fileNameGen + "_merged_" + r + "." + FileTypeConstant.ODT.getName();
					pdfFileStr = xdocUploadService.createPdf(mergeFileStr, odtFiles, host, port);
					pdfFiles.add(pdfFileStr);
					odtFiles.clear();
					LOG.debug("Convert to pdf finished");
				}
			}
			
			if(odtFiles != null && odtFiles.size() > 0) {
				//--: Found the rest odt file so start to merge odt and convert to pdf again
				LOG.debug("Start merge odt and convert to pdf");
				mergeFileStr = filePathTemp + "/" + fileNameGen + "_merged_" + r + "." + FileTypeConstant.ODT.getName();
				pdfFileStr = xdocUploadService.createPdf(mergeFileStr, odtFiles, host, port);
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
			return FilenameUtils.getName(mergeFileStr);
		} catch (Exception e) {
			LOG.error(e.toString());
			xdocUploadService.removeTrashFile(filePathTemp, fileNameGen);
			throw e;
		}
	}
	
	private String genRC_1(Map data, String columnArr[], Date date, long countDup) {
		try {
			Map taskDetailFull = (Map)data.get("taskDetailFull");
			Object object = taskDetailFull.get(columnArr[0]);
			String calumnValue = (object == null ? "" : object.toString());
			String dateStr = String.format(Locale.forLanguageTag("th-TH"), "%1$td%1$tm%1$tY", date);
			
			return calumnValue + "-" + String.format("%02d", countDup) + "-" + dateStr;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} 
	}
	
}
