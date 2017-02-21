package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_PAYMENT_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.PaymentFindCriteriaReq;
import com.may.ple.backend.criteria.TraceResultImportFindCriteriaReq;
import com.may.ple.backend.criteria.TraceResultImportFindCriteriaResp;
import com.may.ple.backend.entity.PaymentFile;
import com.may.ple.backend.entity.TraceResultImportFile;
import com.may.ple.backend.entity.TraceWork;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.model.GeneralModel1;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;
import com.may.ple.backend.utils.Stringutil;

@Service
public class TraceResultImportService {
	private static final Logger LOG = Logger.getLogger(TraceResultImportService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	@Value("${file.path.payment}")
	private String filePathPayment;
	
	@Autowired
	public TraceResultImportService(DbFactory dbFactory, MongoTemplate templateCenter) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
	}
	
	public TraceResultImportFindCriteriaResp find(TraceResultImportFindCriteriaReq req) throws Exception {
		try {
			TraceResultImportFindCriteriaResp resp = new TraceResultImportFindCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = new Criteria();
			
			if(req.getEnabled() != null) {
				criteria.and("enabled").is(req.getEnabled());
			}
			
			Query query = Query.query(criteria);
			
			long totalItems = template.count(query, PaymentFile.class);
			
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage())).with(new Sort(Direction.DESC, "createdDateTime"));
			
			List<PaymentFile> files = template.find(query, PaymentFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String currentProduct) throws Exception {		
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
			
			Sheet sheet = workbook.getSheetAt(0);
			
			LOG.debug("Get Header of excel file");
			Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeaderIndex(sheet);
			
			Users user = ContextDetailUtil.getCurrentUser(templateCenter);
			MongoTemplate template = dbFactory.getTemplates().get(currentProduct);
			
			LOG.debug("Save new file");
			TraceResultImportFile file = new TraceResultImportFile(fd.fileName, date);
			file.setCreatedBy(user.getId());
			file.setUpdateedDateTime(date);
			template.insert(file);
			
			LOG.debug("Save Details");
			GeneralModel1 saveResult = saveDetail(sheet, template, headerIndex);
			
			if(saveResult.rowNum == -1) {
				LOG.debug("Remove taskFile because Saving TaskDetail Error.");
				template.remove(file);
				throw new CustomerException(4001, "Cann't save taskdetail.");
			}
			
			//--: update rowNum to TaskFile.
			file.setRowNum(saveResult.rowNum);
			template.save(file);
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
			if(fileOut != null) fileOut.close();
		}
	}
	
	public Map<String, String> getFile(PaymentFindCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			PaymentFile paymentFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), PaymentFile.class);
			
			String filePath = filePathPayment + "/" + paymentFile.getFileName();
			
			Map<String, String> map = new HashMap<>();
			map.put("filePath", filePath);
			map.put("fileName", paymentFile.getFileName());
			
			return  map;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteFileTask(String productId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			PaymentFile paymentFile = template.findOne(Query.query(Criteria.where("id").is(id)), PaymentFile.class);
			template.remove(paymentFile);
			template.remove(Query.query(Criteria.where(SYS_FILE_ID.getName()).is(id)), NEW_PAYMENT_DETAIL.getName());
			
			if(!new File(filePathPayment + "/" + paymentFile.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + paymentFile.getFileName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private GeneralModel1 saveDetail(Sheet sheetAt, MongoTemplate template, Map<String, Integer> headerIndex) {
		GeneralModel1 result = new GeneralModel1();
		
		try {
			LOG.debug("Start save taskDetail");
			Set<String> keySet = headerIndex.keySet();
			
			List<TraceWork> traceWorks = new ArrayList<>();
			TraceWork traceWork;
			
			Map<String, Object> data;
			boolean isLastRow;
			int r = 1; //--: Start with row 1 for skip header row.
			Cell cell;
			Row row;
			
			while(true) {
				row = sheetAt.getRow(r);
				if(row == null) {
					r--;
					break;
				}
				
				data = new LinkedHashMap<>();
				isLastRow = true;
				
				for (String key : keySet) {
					cell = row.getCell(headerIndex.get(key), MissingCellPolicy.RETURN_BLANK_AS_NULL);
					
					if(cell != null) {
						switch(cell.getCellType()) {
						case Cell.CELL_TYPE_STRING: {
							data.put(key, Stringutil.removeWhitespace(cell.getStringCellValue())); 
							break;
						}
						case Cell.CELL_TYPE_BOOLEAN: {
							data.put(key, cell.getBooleanCellValue());
							break;
						}
						case Cell.CELL_TYPE_NUMERIC: {
								if(HSSFDateUtil.isCellDateFormatted(cell)) {
									data.put(key, cell.getDateCellValue());
								} else {
									data.put(key, cell.getNumericCellValue()); 
								}
								break;															
							}
						default: throw new Exception("Error on column: " + key);
						}
						
						isLastRow = false;
					} else {
						data.put(key, null);
					}
				}			
				
				//--: Break
				if(isLastRow) {
					r--;
					break;
				}
				
				//--: Add row
				traceWork = new TraceWork();
				BeanUtils.populate(traceWork, data);
				
				traceWorks.add(traceWork);
				r++;
			}
			
			template.insert(traceWorks, TraceWork.class);
			result.rowNum = r;
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			result.rowNum = -1;
			return result;
		}
	}
	
}
