package com.may.ple.backend.service;

import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_IS_ACTIVE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OLD_ORDER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_UPDATED_DATE_TIME;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.NewTaskCriteriaReq;
import com.may.ple.backend.criteria.NewTaskCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.GroupData;
import com.may.ple.backend.entity.IsActive;
import com.may.ple.backend.entity.NewTaskFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.model.GeneralModel1;
import com.may.ple.backend.utils.ContextDetailUtil;

@Service
public class NewTaskService {
	private static final Logger LOG = Logger.getLogger(NewTaskService.class.getName());
	private static final int INIT_GROUP_ID = 1;
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
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
	
	private FileDetail getFileName(FormDataContentDisposition fileDetail, Date date) throws Exception {
		try {
			LOG.debug("Start");
			int indexFile = fileDetail.getFileName().lastIndexOf(".");
			String fileName = new String(fileDetail.getFileName().substring(0, indexFile).getBytes("iso-8859-1"), "UTF-8");
			String fileExt = fileDetail.getFileName().substring(indexFile);
			fileName = fileName + "_" + String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", date) + fileExt;
			
			FileDetail fd = new FileDetail();
			fd.fileName = fileName;
			fd.fileExt = fileExt;
			
			LOG.debug("End");
			return fd;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private Map<String, Integer> getFileHeader(Sheet sheet, List<ColumnFormat> columnFormats) {
		try {
			Map<String, Integer> headerIndex = new LinkedHashMap<>();
			int maxOrder = columnFormats.size();
			Row row = sheet.getRow(0);
			ColumnFormat colForm;
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
					value = cell.getStringCellValue().trim().replaceAll("\\.", "");
					headerIndex.put(value, cellIndex - 1);
				}
				
				isContain = false;
				
				for (ColumnFormat c : columnFormats) {
					if(value.equals(c.getColumnName())) {						
						isContain = true;
						break;
					}
				}
				
				if(!isContain) {
					colForm = new ColumnFormat(value, false);
					colForm.setDetGroupId(INIT_GROUP_ID);
					colForm.setDetIsActive(true);
					colForm.setDetOrder(++maxOrder);
					columnFormats.add(colForm);
				}
			}
			
			return headerIndex;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	@SuppressWarnings("resource")
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String currentProduct) throws Exception {
		Workbook workbook = null;
		MongoTemplate template = null;
		
		try {
			List<GroupData> groupDatas = null;
			
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			
			LOG.debug("Get Filename");
			FileDetail fd = getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			if(fd.fileExt.equals(".xlsx")) {
				workbook = new XSSFWorkbook(uploadedInputStream);
			} else if(fd.fileExt.equals(".xls")) {
				workbook = new HSSFWorkbook(uploadedInputStream);
			} else {
				throw new CustomerException(5000, "Filetype not match");
			}
			
			Sheet sheet = workbook.getSheetAt(0);
			
			LOG.debug("Get product");
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(currentProduct)), Product.class);
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			
			LOG.debug("Get db connection");
			template = dbFactory.getTemplates().get(currentProduct);
			
			if(columnFormats == null) {
				if(!template.collectionExists("newTaskDetail")) {
					LOG.debug("Create collection newTaskDetail");
					template.createCollection("newTaskDetail");
				}
				
				template.indexOps("newTaskDetail").ensureIndex(new Index().on(SYS_FILE_ID.getName(), Direction.ASC));
				template.indexOps("newTaskDetail").ensureIndex(new Index().on(SYS_IS_ACTIVE.getName(), Direction.ASC));
				template.indexOps("newTaskDetail").ensureIndex(new Index().on(SYS_OLD_ORDER.getName(), Direction.ASC));
				columnFormats = new ArrayList<>();
			}
			
			if(columnFormats.size() == 0) {
				LOG.debug("Add " + SYS_OWNER.getName() + " column");
				ColumnFormat colForm = new ColumnFormat(SYS_OWNER.getName(), false);
				colForm.setColumnNameAlias("Collector");
				colForm.setDataType(SYS_OWNER.getName());
				colForm.setDetGroupId(INIT_GROUP_ID);
				colForm.setDetIsActive(true);
				colForm.setDetOrder(1);
				columnFormats.add(colForm);	
				
				GroupData groupData = new GroupData();
				groupData.setId(INIT_GROUP_ID);
				groupData.setName("ข้อมูลหลัก");
				
				groupDatas = new ArrayList<>();
				groupDatas.add(groupData);
			}
			
			LOG.debug("Get Header of excel file");
			Map<String, Integer> headerIndex = getFileHeader(sheet, columnFormats);
			
			if(headerIndex.size() > 0) {					
				LOG.debug("Call getCurrentUser");
				Users user = ContextDetailUtil.getCurrentUser(templateCenter);
				
				LOG.debug("Save new TaskFile");
				NewTaskFile taskFile = new NewTaskFile(fd.fileName, date);
				taskFile.setCreatedBy(user.getId());
				template.insert(taskFile);
				
				LOG.debug("Save Task Details");
				GeneralModel1 result = saveTaskDetail(sheet, template, headerIndex, taskFile.getId());
				
				if(result.rowNum == -1) {
					LOG.debug("Remove taskFile because Saving TaskDetail Error.");
					template.remove(taskFile);
					throw new CustomerException(4001, "Cann't save taskdetail.");
				}
				
				//--: Set datatype
				Map<String, String> dataTypes = result.dataTypes;
				Set<String> dataTypeKey = dataTypes.keySet();
				for (String key : dataTypeKey) {
					for (ColumnFormat c : columnFormats) {
						if(key.equals(c.getColumnName())) {
							if(c.getDataType() == null) {
								c.setDataType(dataTypes.get(key));
							}
							break;
						}
					}
				}
				
				//--: update rowNum to TaskFile.
				taskFile.setRowNum(result.rowNum);
				template.save(taskFile);
				
				LOG.debug("Update Product setting");
				if(groupDatas != null) {
					product.setGroupDatas(groupDatas);					
				}
				product.setColumnFormats(columnFormats);
				templateCenter.save(product);
				
				//--: Save to disk for download purpose.
				LOG.debug("Start Thread saving file");
				new SaveFileService(workbook, filePathTask, fd.fileName).start();
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private GeneralModel1 saveTaskDetail(Sheet sheetAt, MongoTemplate template, Map<String, Integer> headerIndex, String taskFileId) {
		GeneralModel1 result = new GeneralModel1();
		
		try {
			LOG.debug("Start save taskDetail");
			Date date = Calendar.getInstance().getTime();
			Set<String> keySet = headerIndex.keySet();
			List<Map<String, Object>> datas = new ArrayList<>();
			Map<String, String> dataTypes = new HashMap<>();
			Map<String, Object> data;
			List<Map<String, String>> owners;
			Map<String, String> owner;
			Row row;
			int r = 1; //--: Start with row 1 for skip header row.
			Cell cell;
			boolean isLastRow;
			String dtt;
			String[] names;
			
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
							if(key.equals(SYS_OWNER.getName())) {								
								owners = new ArrayList<>();
								owner = new HashMap<>();
								names = cell.getStringCellValue().split(",");
								owner.put("showname", names[0]);
								owner.put("username", names[1]);
								owners.add(owner);
									
								data.put(key, owners); 
								dtt = SYS_OWNER.getName();			
							} else {
								data.put(key, cell.getStringCellValue()); 
								dtt = "str";			
							}
							break;
						}
						case Cell.CELL_TYPE_BOOLEAN: {
							data.put(key, cell.getBooleanCellValue());
							dtt = "bool";
							break;
						}
						case Cell.CELL_TYPE_NUMERIC: {
								if(HSSFDateUtil.isCellDateFormatted(cell)) {
									data.put(key, cell.getDateCellValue());
									dtt = "date";
								} else {
									data.put(key, cell.getNumericCellValue()); 
									dtt = "num";
								}
								break;															
							}
						default: throw new Exception("Error on column: " + key);
						}
						
						if(!dataTypes.containsKey(key)) dataTypes.put(key, dtt);
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
				data.put(SYS_FILE_ID.getName(), taskFileId);
				data.put(SYS_OLD_ORDER.getName(), r);
				data.put(SYS_IS_ACTIVE.getName(), new IsActive(true, ""));
				data.put(SYS_CREATED_DATE_TIME.getName(), date);
				data.put(SYS_UPDATED_DATE_TIME.getName(), date);
				datas.add(data);
				r++;
			}
			
			template.insert(datas, "newTaskDetail");
			result.rowNum = r;
			result.dataTypes = dataTypes;
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			result.rowNum = -1;
			return result;
		}
	}
	
	public void deleteFileTask(String currentProduct, String id) throws Exception {
		try {
			
			MongoTemplate template = dbFactory.getTemplates().get(currentProduct);
			NewTaskFile taskFile = template.findOne(Query.query(Criteria.where("id").is(id)), NewTaskFile.class);
			template.remove(taskFile);
			template.remove(Query.query(Criteria.where(SYS_FILE_ID.getName()).is(id)), "newTaskDetail");
			
			if(!new File(filePathTask + "/" + taskFile.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + taskFile.getFileName());
			}
			
			long taskNum = template.count(new Query(), NewTaskFile.class);
			
			if(taskNum == 0) {
				LOG.debug("Task is empty so remove ColumnFormats also");
				Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(currentProduct)), Product.class);
				product.setColumnFormats(null);
				product.setGroupDatas(null);
				templateCenter.save(product);
				
				//--
				template.indexOps("newTaskDetail").dropAllIndexes();
			}
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
