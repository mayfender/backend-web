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
import com.may.ple.backend.constant.FileTypeConstant;
import com.may.ple.backend.criteria.BatchNoticeFindCriteriaResp;
import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.NoticeUpdateCriteriaReq;
import com.may.ple.backend.criteria.NoticeXDocFindCriteriaResp;
import com.may.ple.backend.criteria.TraceResultImportFindCriteriaReq;
import com.may.ple.backend.entity.BatchNoticeFile;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.NoticeXDocFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.TraceWork;
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
	private TraceResultImportService traceResultImportServ;
	private MongoTemplate templateCenter;
	private DbFactory dbFactory;
	private UserAction userAct;
	@Value("${file.path.notice}")
	private String filePathNotice;
	@Value("${file.path.temp}")
	private String filePathTemp;
	
	@Autowired
	public NoticeXDocUploadService(DbFactory dbFactory, MongoTemplate templateCenter, 
				UserAction userAct, TraceResultImportService traceResultImportServ) {
		this.traceResultImportServ = traceResultImportServ;
		this.templateCenter = templateCenter;
		this.dbFactory = dbFactory;
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
			String addrResult;
			String filePath;
			String cellVal;
			Field fields;
			Query query;
			byte[] data;
			Map userMap;
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
							taskDetail.put("owner_fullname", (userMap.get("firstName") == null ? "" : userMap.get("firstName")) + " " + (userMap.get("lastName") == null ? "" : userMap.get("lastName")));
							taskDetail.put("owner_tel", userMap.get("phone"));
						}
					} else if(key.startsWith("address")) {
						addrResult += ("\n" + cellVal);
					} else if(key.equals("noticeTemplateName")) {
						noticeTemplateName = cellVal;
					} else if(key.equals("customerName")) {
						customerName = cellVal;
					}
				} //-- End for
				
				if(taskDetail == null) {
					r-=2;
					break;
				}
				
				taskDetail.put("address_sys", addrResult.trim());
				taskDetail.put("today_sys", printDate == null ? now : printDate);
				taskDetail.put("customer_name_sys", customerName);
				
				LOG.debug("Get file");
				req = new NoticeFindCriteriaReq();
				req.setProductId(productId);
				req.setTemplateName(noticeTemplateName);
				map = getNoticeFile(req);
				filePath = map.get("filePath");
				
				LOG.debug("call XDocUtil.generate");
				data = XDocUtil.generate(filePath, taskDetail);
				
				LOG.debug("Call saveToFile");
				generatedFilePath = saveToFile(filePathTemp, fd.fileName + "_" + (r-1), FilenameUtils.getExtension(filePath), data);
				odtFiles.add(generatedFilePath);
				
				if(((r-1) % 100) == 0) {
					LOG.debug("r = " + r + " so start to merge odt and convert to pdf");
					mergeFileStr = filePathTemp + "/" + fd.fileName + "_merged_" + (r-1) + "." + FileTypeConstant.ODT.getName();
					pdfFileStr = createPdf(mergeFileStr, odtFiles);
					pdfFiles.add(pdfFileStr);
					odtFiles.clear();
					LOG.debug("Convert to pdf finished");
				}
			} //-End while
			
			if(odtFiles != null && odtFiles.size() > 0) {
				//--: Found the rest odt file so start to merge odt and convert to pdf again
				LOG.debug("Start merge odt and convert to pdf");
				mergeFileStr = filePathTemp + "/" + fd.fileName + "_merged_" + r + "." + FileTypeConstant.ODT.getName();
				pdfFileStr = createPdf(mergeFileStr, odtFiles);
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
			
			LOG.info("Start call save traceWork");
			traceResultImportServ.saveDetail(sheet, template, headerIndex, file.getId(), productId);
			LOG.info("End call save traceWork");
			
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
			template.remove(Query.query(Criteria.where("fileId").is(id)), TraceWork.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String createPdf(String mergeFileStr, List<String> odtFiles) throws Exception {
		try {
			XDocUtil.mergeAndRemove(odtFiles, mergeFileStr);
			FileInputStream mergeFile = null;
			String pdfFile = "";
			byte data[];
			
			try {
				LOG.info("Start Convert to pdf");
				mergeFile = new FileInputStream(mergeFileStr);
				data = JodConverterUtil.toPdf(mergeFile, FileTypeConstant.ODT.getName());
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
	
}
