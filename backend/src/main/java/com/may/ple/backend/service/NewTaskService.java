package com.may.ple.backend.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.may.ple.backend.constant.FileTypeConstant;
import com.may.ple.backend.criteria.NewTaskCriteriaReq;
import com.may.ple.backend.criteria.NewTaskCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.NewTaskFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;

@Service
public class NewTaskService {
	private static final Logger LOG = Logger.getLogger(NewTaskService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	private int lastRowNum;
	@Value("${file.path.task}")
	private String filePathTask;
	
	@Autowired
	public NewTaskService(DbFactory dbFactory, MongoTemplate templateCenter) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
	}
	
	public NewTaskCriteriaResp findAll(NewTaskCriteriaReq req) throws Exception {
		try {
			NewTaskCriteriaResp resp = new NewTaskCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getCurrentProduct());
			long totalItems = template.count(new Query(), NewTaskFile.class);
			
			Query query = new Query()
						  .with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			 			  .with(new Sort(Direction.DESC, "createdDateTime"));
			
			List<NewTaskFile> files = template.find(query, NewTaskFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void saveToDisk(Workbook workbook, String fileNameFull) throws Exception {
		try (FileOutputStream fileOut = new FileOutputStream(filePathTask + "/" + fileNameFull)){
			LOG.debug("Start save file");
			File file = new File(filePathTask);
			
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder result: " + result);
			}
			LOG.debug("Save to file");
			
			workbook.write(fileOut);
		
			LOG.debug("Finished save file");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String currentProduct) throws Exception {
		Workbook workbook = null;
		MongoTemplate template = null;
		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			int indexFile = fileDetail.getFileName().lastIndexOf(".");
			String fileName = new String(fileDetail.getFileName().substring(0, indexFile).getBytes("iso-8859-1"), "UTF-8");
			String fileExt = fileDetail.getFileName().substring(indexFile);
			String fileNameFull = fileName + "_" + String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", date) + fileExt;
			FileTypeConstant fileType;
			
			if(fileExt.equals(".xlsx")) {
				workbook = new XSSFWorkbook(uploadedInputStream);
				fileType = FileTypeConstant.XLSX;
			} else if(fileExt.equals(".xls")) {
				workbook = new HSSFWorkbook(uploadedInputStream);
				fileType = FileTypeConstant.XLS;
			} else {
				throw new CustomerException(5000, "Filetype not match");
			}
			
			LOG.debug("Get product");
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(currentProduct)), Product.class);
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			
			if(columnFormats == null) columnFormats = new ArrayList<>();
			
			Map<String, Integer> headerIndex = new LinkedHashMap<>();
			Sheet sheetAt = workbook.getSheetAt(0);
			Row row = sheetAt.getRow(0);
			int cellIndex = 0;
			int countNull = 0;
			boolean isContain;
			String value;
			Cell cell;
			
			while(true) {
				cell = row.getCell(cellIndex++, MissingCellPolicy.RETURN_BLANK_AS_NULL);				
				
				if(countNull == 10) break;
			
				if(cell == null) {
					countNull++;
					continue;
				} else {
					countNull = 0;
					headerIndex.put(cell.getStringCellValue().trim(), cellIndex - 1);
				}
				
				value = cell.getStringCellValue().trim();
				isContain = false;
				
				for (ColumnFormat c : columnFormats) {
					if(value.equals(c.getColumnName())) {						
						isContain = true;
						break;
					}
				}
				
				if(!isContain) {
					columnFormats.add(new ColumnFormat(cell.getStringCellValue().trim()));										
				}
			}
			
			product.setColumnFormats(columnFormats);
			
			if(headerIndex.size() > 0) {
				template = dbFactory.getTemplates().get(currentProduct);
				
				LOG.debug("Get db connection by product and save new TaskFile");
				NewTaskFile taskFile = new NewTaskFile(fileNameFull, date);
				template.insert(taskFile);			
				
				LOG.debug("Update columnFormats of Product");
				templateCenter.save(product);
				
				LOG.debug("Save Task Details");
				int rowNum = saveTaskDetail(fileType, sheetAt, template, headerIndex, taskFile.getId());
				
				//--: update rowNum to TaskFile.
				taskFile.setRowNum(rowNum);
				template.save(taskFile);
				
				//--: Save to disk for download purpose.
				LOG.debug("Save to file");
				saveToDisk(workbook, fileNameFull);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
		}
	}
	
	private int saveTaskDetail(FileTypeConstant fileType, Sheet sheetAt, MongoTemplate template, Map<String, Integer> headerIndex, String taskFileId) {
		try {
			LOG.debug("Last Row : "  + sheetAt.getLastRowNum());
			Set<String> keySet = headerIndex.keySet();
			List<Map<String, Object>> datas = new ArrayList<>();
			Map<String, Object> data;
			Row row;
			Cell cell;
			boolean isLastRow;
			int r = 1; //--: Start with row 1 for skip header row.
			
			for (; r < sheetAt.getLastRowNum(); r++) { 
				row = sheetAt.getRow(r);
				data = new LinkedHashMap<>();
				isLastRow = true;
				
				for (String key : keySet) {
					cell = row.getCell(headerIndex.get(key), MissingCellPolicy.RETURN_BLANK_AS_NULL);
					
					if(cell != null) {
						switch(cell.getCellType()) {
						case Cell.CELL_TYPE_STRING: data.put(key, cell.getStringCellValue()); break;
						case Cell.CELL_TYPE_BOOLEAN: data.put(key, cell.getBooleanCellValue()); break;
						case Cell.CELL_TYPE_NUMERIC: {
								if(HSSFDateUtil.isCellDateFormatted(cell)) {
									data.put(key, cell.getDateCellValue());
								} else {
									data.put(key, cell.getNumericCellValue()); 
								}
								break;															
							}
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
				data.put("taskFileId", taskFileId);
				datas.add(data);
			}
			
			template.insert(datas, "newTaskDetail");
			
			return r;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteFileTask(String currentProduct, String id) throws Exception {
		try {
			
			MongoTemplate template = dbFactory.getTemplates().get(currentProduct);
			NewTaskFile taskFile = template.findOne(Query.query(Criteria.where("id").is(id)), NewTaskFile.class);
			template.remove(taskFile);
			template.remove(Query.query(Criteria.where("taskFileId").is(id)), "newTaskDetail");
			
			if(!new File(filePathTask + "/" + taskFile.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + taskFile.getFileName());
			}
			
			long taskNum = template.count(new Query(), NewTaskFile.class);
			
			if(taskNum == 0) {
				LOG.debug("Task is empty so remove ColumnFormats also");
				Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(currentProduct)), Product.class);
				product.setColumnFormats(null);
				templateCenter.save(product);
			}
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
