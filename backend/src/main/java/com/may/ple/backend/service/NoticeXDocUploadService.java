package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.constant.FileTypeConstant;
import com.may.ple.backend.criteria.BatchNoticeFindCriteriaResp;
import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.NoticeUpdateCriteriaReq;
import com.may.ple.backend.criteria.NoticeXDocFindCriteriaResp;
import com.may.ple.backend.criteria.SaveToPrintCriteriaReq;
import com.may.ple.backend.criteria.TraceResultImportFindCriteriaReq;
import com.may.ple.backend.entity.BatchNoticeFile;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.NoticeToPrint;
import com.may.ple.backend.entity.NoticeXDocFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;
import com.may.ple.backend.utils.JodConverterUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.may.ple.backend.utils.PdfUtil;
import com.may.ple.backend.utils.StringUtil;
import com.may.ple.backend.utils.XDocUtil;

@Service
public class NoticeXDocUploadService {
	private static final Logger LOG = Logger.getLogger(NoticeXDocUploadService.class.getName());
	private MongoTemplate templateCenter;
	private NoticeManagerService noticeServ;
	private DbFactory dbFactory;
	private UserAction userAct;
	@Value("${file.path.notice}")
	private String filePathNotice;
	@Value("${file.path.temp}")
	private String filePathTemp;
	
	@Autowired
	public NoticeXDocUploadService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct) {
		this.templateCenter = templateCenter;
		this.dbFactory = dbFactory;
		this.userAct = userAct;
	}
	
	public void setNoticeServ(NoticeManagerService noticeServ) {
		this.noticeServ = noticeServ;
	}
	
	public NoticeXDocFindCriteriaResp find(NoticeFindCriteriaReq req) throws Exception {
		try {
			NoticeXDocFindCriteriaResp resp = new NoticeXDocFindCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = new Criteria();
			
			if(req.getId() != null) {
				criteria.and("id").is(req.getId());
			}
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
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String currentProduct, String templateName, String id) throws Exception {		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			
			LOG.debug("Get Filename");
			FileDetail fd = FileUtil.getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			
			Users user = ContextDetailUtil.getCurrentUser(templateCenter);
			MongoTemplate template = dbFactory.getTemplates().get(currentProduct);
			NoticeXDocFile noticeFile;
			if(id == null) {
				LOG.debug("Save new TaskFile");
				noticeFile = new NoticeXDocFile(fd.fileName, templateName, date);
				noticeFile.setCreatedBy(user.getId());
				noticeFile.setUpdateedDateTime(date);
				noticeFile.setEnabled(true);
				noticeFile.setIsDateInput(false);
				noticeFile.setFilePath(filePathNotice + "/" + currentProduct);
			} else {
				noticeFile = template.findOne(Query.query(Criteria.where("id").is(id)), NoticeXDocFile.class);
				deleteFile(noticeFile.getFilePath(), noticeFile.getFileName());
				
				noticeFile.setFileName(fd.fileName);
				noticeFile.setUpdateedDateTime(date);
				noticeFile.setUpdatedBy(user.getId());
			}
			template.save(noticeFile);
			
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
	
	public Map<String, String> getNoticeFile(NoticeFindCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			NoticeXDocFile noticeFile;
			
			if(!StringUtils.isBlank(req.getId())) {
				noticeFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), NoticeXDocFile.class);				
			} else if(!StringUtils.isBlank(req.getTemplateName())) {
				noticeFile = template.findOne(Query.query(Criteria.where("templateName").is(req.getTemplateName())), NoticeXDocFile.class);
			} else {
				noticeFile = template.findOne(Query.query(Criteria.where("enabled").is(true)), NoticeXDocFile.class);
			}
			
			if(noticeFile == null) return null;
			
			String filePath;
			
			if(!StringUtils.isBlank(noticeFile.getFilePath())) {				
				filePath = noticeFile.getFilePath() + "/" + noticeFile.getFileName();				
			} else {
				filePath = filePathNotice + "/" + noticeFile.getFileName();				
			}
			
			Map<String, String> map = new HashMap<>();
			map.put("id", noticeFile.getId());
			map.put("filePath", filePath);
			map.put("fileName", noticeFile.getFileName());
			map.put("fields", noticeFile.getMore() != null ? noticeFile.getMore().get("fields").toString() : null);
			
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
			
			deleteFile(noticeFile.getFilePath(), noticeFile.getFileName());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String uploadBatchNotice(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, FileDetail fd, String productId) throws Exception {
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
			
			Date date = Calendar.getInstance().getTime();
			Users user = ContextDetailUtil.getCurrentUser(templateCenter);
			
			//-------------
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			BatchNoticeFile file = new BatchNoticeFile(fd.fileName, date);
			file.setCreatedBy(user.getId());
			file.setUpdateedDateTime(date);
			template.insert(file);
			//--------------
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			String contractNoColumnName = productSetting.getContractNoColumnName();
			String host = productSetting.getOpenOfficeHost();
			Integer port = productSetting.getOpenOfficePort();
			List<Users> users = userAct.getUserByProductToAssign(productId).getUsers();
			
			Sheet sheet = workbook.getSheetAt(0);
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			List<ColumnFormat> columnFormats = new ArrayList<>();
			Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet, columnFormats);
			if(headerIndex.size() == 0) {		
				LOG.error("Not found Headers");
				throw new Exception("Not found Headers");
			}
			
			Map<String, Map<String, String>> templateName = new HashMap<>();
			Date now = Calendar.getInstance().getTime();
			Set<String> keySet = headerIndex.keySet();
			List<String> odtFiles = new ArrayList<>();
			List<String> pdfFiles = new ArrayList<>();
			String mergeFileStr = "", pdfFileStr = "";
			List<Map<String, String>> userList;
			Date dateVal = null, printDate;
			String noticeTemplateName;
			NoticeFindCriteriaReq req;
			String generatedFilePath;
			String columns[] = null;
			Map<String, String> map;
			Map taskDetail = null;
			List<String> ownerId;
			String customerName;
			String contractNo;
			String addrResult;
			String filePath;
			String cellVal;
			Field fields;
			Query query;
			byte[] data;
			Map userMap;
			Map others;
			Cell cell;
			int r = 1;
			Row row;
			
			while(true) {
				row = sheet.getRow(r++);
				
				if(row == null) {
					r-=2;
					break;
				}
				
				addrResult = "";
				printDate = null;
				noticeTemplateName = null;
				taskDetail = null;
				customerName = null;
				others = new HashMap();
				dateVal = null;
				cellVal = null;
				contractNo = null;
				
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
							cellVal = new DataFormatter(Locale.ENGLISH).formatCellValue(cell);
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
						contractNo = cellVal;
					} else if(key.startsWith("address")) {
						addrResult += ("\n" + cellVal);
					} else if(key.equals("noticeTemplateName")) {
						noticeTemplateName = cellVal;
					} else if(key.equals("customerName")) {
						customerName = cellVal;
					} else {
						if(dateVal == null) {
							others.put(key, cellVal);						
						} else {
							others.put(key, new DataFormatter(Locale.ENGLISH).formatCellValue(cell));
						}
					}
				} //-- End for
				
				if(contractNo == null) continue;
				
				if(!templateName.containsKey(noticeTemplateName)) {
					LOG.debug("Get file");
					req = new NoticeFindCriteriaReq();
					req.setProductId(productId);
					req.setTemplateName(noticeTemplateName);					
					templateName.put(noticeTemplateName, getNoticeFile(req));					
				}
				
				map = templateName.get(noticeTemplateName);
				query = Query.query(Criteria.where(contractNoColumnName).is(contractNo));
				fields = query.fields().include(SYS_OWNER_ID.getName());
				
				if(map.get("fields") != null && !map.get("fields").isEmpty()) {
					columns = map.get("fields").toString().split(",");
				}
				for (String col : columns) fields.include(col);
				
				taskDetail = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
				if(taskDetail == null) {
					r-=2;
					break;
				}
				
				ownerId = (List)taskDetail.get(SYS_OWNER_ID.getName());
				userList = MappingUtil.matchUserId(users, ownerId.get(0));
				
				if(userList != null && userList.size() > 0) {
					userMap = (Map)userList.get(0);
					taskDetail.put("owner_fullname", userMap.get("firstName") + " " + userMap.get("lastName"));
					taskDetail.put("owner_fullname", (userMap.get("firstName") == null ? "" : userMap.get("firstName")) + " " + (userMap.get("lastName") == null ? "" : userMap.get("lastName")));
					taskDetail.put("owner_tel", userMap.get("phone") == null ? "" : userMap.get("phone"));
					taskDetail.put("owner_tel_ext", userMap.get("phoneExt") == null ? "" : userMap.get("phoneExt"));
				}
				
				taskDetail.put("address_sys", addrResult.trim());
				taskDetail.put("today_sys", printDate == null ? now : printDate);
				taskDetail.put("customer_name_sys", customerName);
				taskDetail.put("noticeTemplateName", noticeTemplateName);
				taskDetail.putAll(others);
				
				filePath = map.get("filePath");
				LOG.debug("call XDocUtil.generate");
				data = XDocUtil.generate(filePath, taskDetail);
				
				LOG.debug("Call saveToFile");
				generatedFilePath = saveToFile(filePathTemp, fd.fileName + "_" + (r-1), FilenameUtils.getExtension(filePath), data);
				odtFiles.add(generatedFilePath);
				
				//--: Save noticeToPrint collection.
				SaveToPrintCriteriaReq printReq = new SaveToPrintCriteriaReq();
				printReq.setProductId(productId);
				printReq.setAddress(addrResult.trim());
				printReq.setCustomerName(customerName);
				printReq.setNoticeId(map.get("id"));
				printReq.setTaskDetailId(taskDetail.get("_id").toString());
				printReq.setPrintStatus(true);
				printReq.setBatchNoticeFileId(file.getId());
				noticeServ.saveToPrint(printReq);
				
				if(((r-1) % 100) == 0) {
					LOG.debug("r = " + r + " so start to merge odt and convert to pdf");
					mergeFileStr = filePathTemp + "/" + fd.fileName + "_merged_" + (r-1) + "." + FileTypeConstant.ODT.getName();
					pdfFileStr = createPdf(mergeFileStr, odtFiles, host, port);
					pdfFiles.add(pdfFileStr);
					odtFiles.clear();
					LOG.debug("Convert to pdf finished");
				}
			} //-End while
			
			if(odtFiles != null && odtFiles.size() > 0) {
				//--: Found the rest odt file so start to merge odt and convert to pdf again
				LOG.debug("Start merge odt and convert to pdf");
				mergeFileStr = filePathTemp + "/" + fd.fileName + "_merged_" + r + "." + FileTypeConstant.ODT.getName();
				pdfFileStr = createPdf(mergeFileStr, odtFiles, host, port);
				pdfFiles.add(pdfFileStr);
				LOG.debug("Convert to pdf finished");
				
				if(pdfFiles.size() == 1) {
					mergeFileStr = pdfFiles.get(0);					
				} else {					
					mergeFileStr = filePathTemp + "/" + fd.fileName + "_merged." + FileTypeConstant.PDF.getName();
					PdfUtil.mergePdf(pdfFiles, mergeFileStr);
				}
			} else if(pdfFiles != null && pdfFiles.size() > 0) {
				if(pdfFiles.size() == 1) {
					mergeFileStr = pdfFiles.get(0);					
				} else {					
					mergeFileStr = filePathTemp + "/" + fd.fileName + "_merged." + FileTypeConstant.PDF.getName();
					PdfUtil.mergePdf(pdfFiles, mergeFileStr);
				}
			} else {
				LOG.warn("Not found file to gen Notice");
			}
			
			//--: update rowNum to TaskFile.
			file.setRowNum(r);
			template.save(file);
			
			LOG.info("End");
			return FilenameUtils.getName(mergeFileStr);
		} catch (Exception e) {
			LOG.error(e.toString());
			removeTrashFile(filePathTemp, fd.fileName);
			throw e;
		} finally {
			if(workbook != null) workbook.close();
		}
	}
	
	public BatchNoticeFindCriteriaResp findBatchNotice(TraceResultImportFindCriteriaReq req) throws Exception {
		try {
			BatchNoticeFindCriteriaResp resp = new BatchNoticeFindCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = new Criteria();
			
			if(req.getEnabled() != null) {
				criteria.and("enabled").is(req.getEnabled());
			}
			
			Query query = Query.query(criteria);
			
			long totalItems = template.count(query, BatchNoticeFile.class);
			
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage())).with(new Sort(Direction.DESC, "createdDateTime"));
			
			List<BatchNoticeFile> files = template.find(query, BatchNoticeFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteBatchNoticeFile(String productId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			BatchNoticeFile file = template.findOne(Query.query(Criteria.where("id").is(id)), BatchNoticeFile.class);
			template.remove(file);
			template.remove(Query.query(Criteria.where("batchNoticeFileId").is(new ObjectId(file.getId()))), NoticeToPrint.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String createPdf(String mergeFileStr, List<String> odtFiles, String host, Integer port) throws Exception {
		try {
			XDocUtil.mergeAndRemove(odtFiles, mergeFileStr);
			FileInputStream mergeFile = null;
			String pdfFile = "";
			byte data[];
			
			try {
				LOG.info("Start Convert to pdf");
				mergeFile = new FileInputStream(mergeFileStr);
				data = JodConverterUtil.toPdf(mergeFile, FileTypeConstant.ODT.getName(), host, port);
				pdfFile = saveToFile(filePathTemp, FilenameUtils.getBaseName(mergeFileStr), FileTypeConstant.PDF.getName(), data);
			} catch (Exception e) {
				LOG.error(e.toString());
				throw e;
			} finally {
				if(mergeFile != null) mergeFile.close();					
			}
			LOG.info("Start Remove merge file");				
			FileUtils.forceDelete(new File(mergeFileStr));
			
			return pdfFile;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void removeTrashFile(String folder, String fileName) {
		File[] files = new File(folder).listFiles();
		for (File file : files) {
			if(!file.getName().startsWith(fileName)) continue;
			FileUtils.deleteQuietly(file);
		}
	}
	
	public String saveToFile(String path, String fileNameFull, String ext, byte[] data) throws Exception {
		FileOutputStream fileOut = null;
		String filePath;
		
		try {
			LOG.debug("Start save file");
			
			File file = new File(path);
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder SUCCESS!");
			}
			
			filePath = path + "/" + fileNameFull + "." + ext;
			
			fileOut = new FileOutputStream(filePath);
			fileOut.write(data);
		
			LOG.debug("Finished save file");
			return filePath;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(fileOut != null) fileOut.close();
		}
	}
	
	public void updateMore(String productId, String id, String key, Object value) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			Update update = new Update();
			update.set("more." + key, value);
			template.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(id))), update, "noticeXDocFile");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void deleteFile(String path, String name) {
		try {
			String filePath;
			if(!StringUtils.isBlank(path)) {				
				filePath = path + "/" + name;				
			} else {
				filePath = filePathNotice + "/" + name;				
			}
			
			if(!new File(filePath).delete()) {
				LOG.warn("Cann't delete file " + name);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
