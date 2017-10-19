package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_UPDATED_DATE_TIME;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.criteria.FileCommonCriteriaResp;
import com.may.ple.backend.criteria.PaymentFindCriteriaReq;
import com.may.ple.backend.criteria.PaymentOnlineChkCriteriaReq;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.PaymentOnlineCheckFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.model.GeneralModel1;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.DateUtil;
import com.may.ple.backend.utils.ExcelUtil;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

@Service
public class PaymentOnlineCheckService {
	private static final Logger LOG = Logger.getLogger(PaymentOnlineCheckService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	private UserAction userAct;
	
	@Autowired
	public PaymentOnlineCheckService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.userAct = userAct;
	}
	
	public FileCommonCriteriaResp find(PaymentFindCriteriaReq req) throws Exception {
		try {
			FileCommonCriteriaResp resp = new FileCommonCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = new Criteria();
			
			Query query = Query.query(criteria);
			
			long totalItems = template.count(query, PaymentOnlineCheckFile.class);
			
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage())).with(new Sort(Direction.DESC, "createdDateTime"));
			
			query.fields()
			.include("fileName")
			.include("createdDateTime")
			.include("rowNum");
			
			List<Map> files = template.find(query, Map.class, "paymentOnlineCheckFile");			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String productId) throws Exception {		
		Workbook workbook = null;
		FileOutputStream fileOut = null;
		
		try {
			LOG.debug("Start Save");
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
			
			LOG.debug("Get product");
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			ProductSetting setting = product.getProductSetting();
			
			Sheet sheet = workbook.getSheetAt(0);
			Users user = ContextDetailUtil.getCurrentUser(templateCenter);
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			LOG.debug("Save new file");
			PaymentOnlineCheckFile file = new PaymentOnlineCheckFile(fd.fileName, date);
			file.setCreatedBy(user.getId());
			file.setUpdateedDateTime(date);
			template.insert(file);
			
			LOG.debug("Get Header of excel file");
			Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet);
			
			GeneralModel1 saveResult = saveDetail(sheet, template, setting.getContractNoColumnName(), productId, headerIndex, file.getId(), date);								
			
			if(saveResult.rowNum == -1) {
				LOG.debug("Remove file because Saving error.");
				template.remove(file);
				throw new CustomerException(4001, "Cann't save.");
			}

			//--: update rowNum to TaskFile.
			file.setRowNum(saveResult.rowNum);
			template.save(file);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
			if(fileOut != null) fileOut.close();
		}
	}
	
	public void deleteFile(String productId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			PaymentOnlineCheckFile file = template.findOne(Query.query(Criteria.where("id").is(id)), PaymentOnlineCheckFile.class);
			template.remove(file);
			template.remove(Query.query(Criteria.where(SYS_FILE_ID.getName()).is(id)), "paymentOnlineChkDet");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteChkLstItem(String productId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			template.remove(Query.query(Criteria.where("_id").is(id)), "paymentOnlineChkDet");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void addContractNo(PaymentOnlineChkCriteriaReq req) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			
			Date from = DateUtil.getStartDate(date);
			Date to = DateUtil.getEndDate(date);
			
			Criteria criteria = Criteria.where(SYS_CREATED_DATE_TIME.getName()).gte(from).lte(to).and("contractNo").is(req.getContractNo());
			Query query = Query.query(criteria);
			long totalItems = template.count(query, "paymentOnlineChkDet");
			if(totalItems > 0) return;
			
			Map<String, Object> data = new HashMap<>();
			data.put("contractNo", req.getContractNo());
			data.put("status", 1);
			data.put(SYS_CREATED_DATE_TIME.getName(), date);
			data.put(SYS_UPDATED_DATE_TIME.getName(), date);
			
			template.insert(data, "paymentOnlineChkDet");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public FileCommonCriteriaResp getCheckList(PaymentOnlineChkCriteriaReq req) throws Exception {
		try {
			FileCommonCriteriaResp resp = new FileCommonCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting setting = product.getProductSetting();
			List<ColumnFormat> headers = product.getColumnFormats();
			headers = getColumnFormatsActive(headers);
			resp.setHeaders(headers);
			
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			
			Date from = DateUtil.getStartDate(req.getDate());
			Date to = DateUtil.getEndDate(req.getDate());
	        
			Criteria criteria = Criteria.where(SYS_CREATED_DATE_TIME.getName()).gte(from).lte(to);
			MatchOperation match = Aggregation.match(criteria);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(SYS_CREATED_DATE_TIME.getName(), -1));
			
			BasicDBObject fields = new BasicDBObject();
			fields.append(SYS_CREATED_DATE_TIME.getName(), 1);
			fields.append("status", 1);
			fields.append("taskDetailFull." + SYS_OWNER_ID.getName(), 1);
			
			BasicDBObject project = new BasicDBObject("$project", fields);
			for (ColumnFormat columnFormat : headers) {
				fields.append("taskDetailFull." + columnFormat.getColumnName(), 1);				
			}
			
			List<AggregationOperation> aggregateLst = new ArrayList<>();
			aggregateLst.add(match);
			aggregateLst.add(new CustomAggregationOperation(sort));
			aggregateLst.add(new CustomAggregationOperation(
		        new BasicDBObject(
			            "$lookup",
			            new BasicDBObject("from", NEW_TASK_DETAIL.getName())
			                .append("localField", "contractNo")
			                .append("foreignField", setting.getContractNoColumnName())
			                .append("as", "taskDetailFull")
			)));
			aggregateLst.add(new CustomAggregationOperation(project));
		
			//------------: convert a $lookup result to an object instead of array
			fields = new BasicDBObject();
			fields.append(SYS_CREATED_DATE_TIME.getName(), 1);
			fields.append("status", 1);
			
			BasicDBList dbList = new BasicDBList();
			dbList.add("$taskDetailFull");
			dbList.add(0);
			fields.append("taskDetailFull", new BasicDBObject("$arrayElemAt", dbList));
			project = new BasicDBObject("$project", fields);
			//------------: convert a $lookup result to an object instead of array
			
			aggregateLst.add(new CustomAggregationOperation(project));
			
			Aggregation agg = Aggregation.newAggregation(aggregateLst.toArray(new AggregationOperation[aggregateLst.size()]));
			AggregationResults<Map> aggResult = template.aggregate(agg, "paymentOnlineChkDet", Map.class);
			Map<String, List<Map>> checkListGroup = new HashMap<>();
			List<Map> checkList = aggResult.getMappedResults();
			List<Map<String, String>> userList;
			List<String> userIds;
			List<Map> test;
			Map subMap;
			String uId;
			
			for (Map map : checkList) {
				subMap = (Map)map.get("taskDetailFull");
				if(subMap == null) continue;
				
				userIds = (List)subMap.get(SYS_OWNER_ID.getName());
				
				if(userIds == null) continue;
				
				uId = userIds.get(0);
				
				if(StringUtils.isNoneBlank(req.getOwner()) && !req.getOwner().equals(uId)) {
					continue;
				}
				
				userList = MappingUtil.matchUserId(users, uId);
				subMap.put(SYS_OWNER.getName(), userList);
				
				if(checkListGroup.containsKey(map.get("status").toString())) {
					checkListGroup.get(map.get("status").toString()).add(map);
				} else {
					test = new ArrayList<>();
					test.add(map);
					checkListGroup.put(map.get("status").toString(), test);					
				}
			}
			
			resp.setCheckList(checkListGroup);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private GeneralModel1 saveDetail(Sheet sheetAt, MongoTemplate template, String contNoColName, 
										String productId, Map<String, Integer> headerIndex, String fileId, Date date) {		
		GeneralModel1 result = new GeneralModel1();
		
		try {
			LOG.debug("Start save");
			Date from = DateUtil.getStartDate(date);
			Date to = DateUtil.getEndDate(date);
			String contractNoColName = "contractNo";
			List<Map<String, Object>> datas = new ArrayList<>();
			Integer contractNoIndex = headerIndex.get(contractNoColName);
			Map<String, Object> data;
			String contractNo;
			Row row;
			int r = 1; //--: Start with row 1 for skip header row.
			Cell cell;
			boolean isLastRow;
			
			while(true) {
				row = sheetAt.getRow(r);
				if(row == null) {
					r--;
					break;
				}
				
				data = new LinkedHashMap<>();
				isLastRow = true;
				
				cell = row.getCell(contractNoIndex, MissingCellPolicy.RETURN_BLANK_AS_NULL);
				
				if(cell != null) {
					contractNo = ExcelUtil.getValue(cell, "str", null, null).toString();
					
					Criteria criteria = Criteria.where(SYS_CREATED_DATE_TIME.getName()).gte(from).lte(to).and("contractNo").is(contractNo);
					Query query = Query.query(criteria);
					long totalItems = template.count(query, "paymentOnlineChkDet");
					if(totalItems > 0) {
						r++;
						continue;
					}
					
					data.put(contractNoColName, contractNo);
					isLastRow = false;
				} else {
					data.put(contractNoColName, null);
				}
				
				//--: Break
				if(isLastRow) {
					r--;
					break;
				}
				
				//--: Add row
				data.put("status", 1);
				data.put(SYS_FILE_ID.getName(), fileId);
				data.put(SYS_CREATED_DATE_TIME.getName(), date);
				data.put(SYS_UPDATED_DATE_TIME.getName(), date);
				datas.add(data);
								
				r++;
			}
			
			if(datas.size() > 0) {
				template.insert(datas, "paymentOnlineChkDet");				
			}
			result.rowNum = datas.size();
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			result.rowNum = -1;
			return result;
		}
	}
	
	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats) {
		List<ColumnFormat> result = new ArrayList<>();
		int i = 0;
		for (ColumnFormat colFormat : columnFormats) {
			if(i == 5) break;
			
			if(colFormat.getIsActive()) {
				result.add(colFormat);
			}
			i++;
		}
		
		return result;
	}
	
}
