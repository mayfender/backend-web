package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.NoticeUpdateCriteriaReq;
import com.may.ple.backend.criteria.NoticeXDocFindCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.NoticeXDocFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.may.ple.backend.utils.StringUtil;

@Service
public class NoticeXDocUploadService {
	private static final Logger LOG = Logger.getLogger(NoticeXDocUploadService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	@Value("${file.path.notice}")
	private String filePathNotice;
	@Value("${file.path.temp}")
	private String filePathTemp;
	private UserAction userAct;
	
	@Autowired
	public NoticeXDocUploadService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.userAct = userAct;
	}
	
	public NoticeXDocFindCriteriaResp find(NoticeFindCriteriaReq req) throws Exception {
		try {
			NoticeXDocFindCriteriaResp resp = new NoticeXDocFindCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = new Criteria();
			
			if(req.getEnabled() != null) {
				criteria.and("enabled").is(req.getEnabled());
			}
			if(req.getNoticeForms() != null && req.getNoticeForms().size() > 0) {
				criteria.and("templateName").in(req.getNoticeForms());				
			}
			
			Query query = Query.query(criteria);
			
			long totalItems = template.count(query, NoticeXDocFile.class);
			
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
				 .with(new Sort(Direction.DESC, "enabled"))
				 .with(new Sort(Direction.ASC, "templateName"));
			
			List<NoticeXDocFile> files = template.find(query, NoticeXDocFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String currentProduct, String templateName) throws Exception {		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			
			LOG.debug("Get Filename");
			FileDetail fd = FileUtil.getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			
			Users user = ContextDetailUtil.getCurrentUser(templateCenter);
			MongoTemplate template = dbFactory.getTemplates().get(currentProduct);
			
			LOG.debug("Save new TaskFile");
			NoticeXDocFile noticeFile = new NoticeXDocFile(fd.fileName, templateName, date);
			noticeFile.setCreatedBy(user.getId());
			noticeFile.setUpdateedDateTime(date);
			noticeFile.setEnabled(true);
			noticeFile.setIsDateInput(false);
			noticeFile.setFilePath(filePathNotice + "/" + currentProduct);
			template.insert(noticeFile);
			
			File file = new File(noticeFile.getFilePath());
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create folder");
				LOG.debug("Create Folder SUCCESS!");
			}
			
			String filePathStr = noticeFile.getFilePath() + "/" + fd.fileName;
			
			Files.copy(uploadedInputStream, Paths.get(filePathStr));
			LOG.debug("Save finished");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateTemplateName(NoticeUpdateCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			NoticeXDocFile noticeFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), NoticeXDocFile.class);
			noticeFile.setTemplateName(req.getTemplateName());
			
			template.save(noticeFile);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateEnabled(NoticeUpdateCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			NoticeXDocFile noticeFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), NoticeXDocFile.class);
			
			if(noticeFile.getEnabled()) {
				noticeFile.setEnabled(false);
			} else {
				noticeFile.setEnabled(true);
			}
			
			template.save(noticeFile);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateDateInput(NoticeUpdateCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			NoticeXDocFile noticeFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), NoticeXDocFile.class);
			
			if(noticeFile.getIsDateInput() == null || !noticeFile.getIsDateInput()) {
				noticeFile.setIsDateInput(true);
			} else {
				noticeFile.setIsDateInput(false);
			}
			
			template.save(noticeFile);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Map<String, String> getNoticeFile(NoticeFindCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			NoticeXDocFile noticeFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), NoticeXDocFile.class);
			
			if(noticeFile == null) return null;
			
			String filePath;
			
			if(!StringUtils.isBlank(noticeFile.getFilePath())) {				
				filePath = noticeFile.getFilePath() + "/" + noticeFile.getFileName();				
			} else {
				filePath = filePathNotice + "/" + noticeFile.getFileName();				
			}
			
			Map<String, String> map = new HashMap<>();
			map.put("filePath", filePath);
			map.put("fileName", noticeFile.getFileName());
			
			return  map;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteNoticeFile(String productId, String id) throws Exception {
		try {
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			NoticeXDocFile noticeFile = template.findOne(Query.query(Criteria.where("id").is(id)), NoticeXDocFile.class);
			template.remove(noticeFile);
			
			String filePath;
			
			if(!StringUtils.isBlank(noticeFile.getFilePath())) {				
				filePath = noticeFile.getFilePath() + "/" + noticeFile.getFileName();				
			} else {
				filePath = filePathNotice + "/" + noticeFile.getFileName();				
			}
			
			if(!new File(filePath).delete()) {
				LOG.warn("Cann't delete file " + noticeFile.getFileName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String uploadBatchNotice(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, FileDetail fd, String productId) throws Exception {
		ByteArrayOutputStream outputArray = null;
		Workbook workbook = null;
		
		try {
			LOG.debug("File ext: " + fd.fileExt);						
			
			if(fd.fileExt.equals(".xlsx")) {
				workbook = new XSSFWorkbook(uploadedInputStream);
			} else if(fd.fileExt.equals(".xls")) {
				workbook = new HSSFWorkbook(uploadedInputStream);
			} else {
				throw new CustomerException(5000, "Filetype not match");
			}
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			String contractNoColumnName = product.getProductSetting().getContractNoColumnName();
			List<Users> users = userAct.getUserByProductToAssign(productId).getUsers();
			
			Sheet sheet = workbook.getSheetAt(0);
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			List<ColumnFormat> columnFormats = new ArrayList<>();
			Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet, columnFormats);
			if(headerIndex.size() == 0) {		
				LOG.error("Not found Headers");
				throw new Exception("Not found Headers");
			}
			
			Date now = Calendar.getInstance().getTime();
			outputArray = new ByteArrayOutputStream();
			Set<String> keySet = headerIndex.keySet();
			List<Map<String, String>> userList;
			Date dateVal = null, printDate;
			String columns[] = null;
			Map taskDetail = null;
			List<String> ownerId;
			String addrResult;
			String cellVal;
			Field fields;
			Query query;
			Map userMap;
			Cell cell;
			int r = 1;
			Row row;
			
			while(true) {
				row = sheet.getRow(r++);
				
				if(row == null) {
					r--;
					break;
				}
				
				addrResult = "";
				printDate = null;
				
				for (String key : keySet) {
					cell = row.getCell(headerIndex.get(key), MissingCellPolicy.RETURN_BLANK_AS_NULL);
					
					if(cell == null || StringUtils.isBlank(String.valueOf(cell))) continue;	
					
					if(cell.getCellType() == Cell.CELL_TYPE_FORMULA){
						cellVal = StringUtil.removeWhitespace(new DataFormatter().formatCellValue(cell, formulaEvaluator));
					} else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {	
						if(HSSFDateUtil.isCellDateFormatted(cell)) {
							dateVal = cell.getDateCellValue();
							cellVal = "";
						} else {							
							cellVal = NumberToTextConverter.toText(cell.getNumericCellValue());
						}
					} else {
						cellVal = StringUtil.removeWhitespace(new DataFormatter().formatCellValue(cell));	
					}
					
					if(key.equals("columns")) {
						if(columns != null) continue;
						columns = cellVal.split(",");
					} else if(key.equals("printDate")) {
						printDate = dateVal;
					} else if(key.equals("contractNo")) {
						query = Query.query(Criteria.where(contractNoColumnName).is(cellVal));
						fields = query.fields().include(SYS_OWNER_ID.getName());
						
						for (String col : columns) fields.include(col);
						
						taskDetail = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
						if(taskDetail == null) break;
						
						ownerId = (List)taskDetail.get(SYS_OWNER_ID.getName());
						userList = MappingUtil.matchUserId(users, ownerId.get(0));
						
						if(userList != null && userList.size() > 0) {
							userMap = (Map)userList.get(0);
							taskDetail.put("owner_fullname", userMap.get("firstName") + " " + userMap.get("lastName"));
							taskDetail.put("owner_tel", userMap.get("phone"));
						}
					} else if(key.startsWith("address")) {
						addrResult += ("\n" + cellVal);
					}
				}
				
				taskDetail.put("address_sys", addrResult.trim());
				taskDetail.put("today_sys", printDate == null ? now : printDate);
				
				System.out.println(taskDetail);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
		}
		
//		LOG.debug("Call saveToFile");
//		saveToFile(filePathTemp, fd.fileName, type.getExt(), outputArray);
		
		return "odt";
	}
	
	private void saveToFile(String path, String fileNameFull, String ext, ByteArrayOutputStream outputArray) throws Exception {
		FileOutputStream fileOut = null;
		
		try {
			LOG.debug("Start save file");
			
			File file = new File(path);
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder SUCCESS!");
			}
			
			fileOut = new FileOutputStream(path + "/" + fileNameFull + "." + ext);
			fileOut.write(outputArray.toByteArray());
		
			LOG.debug("Finished save file");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(fileOut != null) fileOut.close();
		}
	}
	
}
