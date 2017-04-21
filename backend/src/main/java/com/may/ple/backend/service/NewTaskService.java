package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_APPOINT_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_IS_ACTIVE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_NEXT_TIME_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OLD_ORDER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_TAGS;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_TAGS_U;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_TRACE_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_UPDATED_DATE_TIME;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.bussiness.ImportExcel;
import com.may.ple.backend.criteria.ExportTemplateFindCriteriaResp;
import com.may.ple.backend.criteria.NewTaskCriteriaReq;
import com.may.ple.backend.criteria.NewTaskCriteriaResp;
import com.may.ple.backend.criteria.NewTaskUpdateCriteriaReq;
import com.may.ple.backend.criteria.TraceResultReportFindCriteriaReq;
import com.may.ple.backend.criteria.TraceResultRportUpdateCriteriaReq;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.ExportTemplateFile;
import com.may.ple.backend.entity.GroupData;
import com.may.ple.backend.entity.IsActive;
import com.may.ple.backend.entity.NewTaskFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.model.GeneralModel1;
import com.may.ple.backend.model.Tag;
import com.may.ple.backend.model.YearType;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.ExcelUtil;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;
import com.may.ple.backend.utils.POIExcelUtil;
import com.may.ple.backend.utils.StringUtil;

@Service
public class NewTaskService {
	private static final Logger LOG = Logger.getLogger(NewTaskService.class.getName());
	private static final int INIT_GROUP_ID = 1;
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	@Value("${file.path.task}")
	private String filePathTask;
	@Value("${file.path.exportTemplate}")
	private String filePathExportTemplate;
	
	@Autowired
	public NewTaskService(DbFactory dbFactory, MongoTemplate templateCenter) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
	}
	
	public NewTaskCriteriaResp findAll(NewTaskCriteriaReq req) throws Exception {
		try {
			NewTaskCriteriaResp resp = new NewTaskCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			long totalItems = template.count(new Query(), NewTaskFile.class);
			
			Query query = new Query()
						  .with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			 			  .with(new Sort(Direction.DESC, "createdDateTime"));
			query.fields()
			.include("fileName")
			.include("rowNum")
			.include("insertRowNum")
			.include("enabled")
			.include("createdDateTime")
			.include("updateRowNum");
			
			List<NewTaskFile> files = template.find(query, NewTaskFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	@SuppressWarnings("resource")
	public Map<String, Object> save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, 
									String currentProduct, Boolean isConfirmImport, List<YearType> yearT) throws Exception {
		Workbook workbook = null;
		MongoTemplate template = null;
		
		try {
			List<GroupData> groupDatas = null;
			
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
			POIExcelUtil.removeSheetExcept0(workbook);
			
			LOG.debug("Get product");
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(currentProduct)), Product.class);
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			
			LOG.debug("Get db connection");
			template = dbFactory.getTemplates().get(currentProduct);
			boolean isFirstTime = false;
			
			if(columnFormats == null) {
				if(!template.collectionExists(NEW_TASK_DETAIL.getName())) {
					LOG.debug("Create collection " + NEW_TASK_DETAIL.getName());
					template.createCollection(NEW_TASK_DETAIL.getName());
				}
				
				columnFormats = new ArrayList<>();
			}
			
			Map<String, Integer> headerIndex;
			
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
				isFirstTime = true;
				
				LOG.debug("Get Header of excel file");
				headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet, columnFormats);
			} else {
				LOG.debug("Get Header of excel file");
				headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet);
			}
			
			if(headerIndex.size() > 0) {
				if(!isFirstTime && (isConfirmImport == null || !isConfirmImport)) {
					List<ColumnFormat> colDateTypes = ImportExcel.getColDateType(headerIndex, columnFormats);
					List<String> colNotFounds = ImportExcel.getColNotFound(headerIndex, columnFormats);
					Map<String, Object> colData = new HashMap<>();
					colData.put("colDateTypes", colDateTypes);
					colData.put("colNotFounds", colNotFounds);
					
					if(colDateTypes.size() > 0 || colNotFounds.size() > 0) return colData;
				}
				
				LOG.debug("Call getCurrentUser");
				Users user = ContextDetailUtil.getCurrentUser(templateCenter);
				
				String path = filePathTask + "/" + FileUtil.getPath(currentProduct);
				LOG.debug(path);
				
				LOG.debug("Save new TaskFile");
				NewTaskFile taskFile = new NewTaskFile(fd.fileName, date);
				taskFile.setEnabled(true);
				taskFile.setCreatedBy(user.getId());
				taskFile.setFilePath(path);
				template.insert(taskFile);
				
				LOG.debug("Get All data to check duplicate");
				ProductSetting productSetting = product.getProductSetting();
				String contractNoColumnName = productSetting.getContractNoColumnName();
				List<Map> allContractNo = null;
				if(!StringUtils.isBlank(contractNoColumnName)) {					
					allContractNo = getAllContactNo(template, contractNoColumnName);
				}
				
				GeneralModel1 result = null;
				
				if(isFirstTime) {
					LOG.debug("Save Task Details for fistTime");
					result = saveTaskDetailFirstTime(sheet, template, headerIndex, taskFile.getId(), date, allContractNo, contractNoColumnName);					
				} else {
					LOG.debug("Save Task Details");
					result = saveTaskDetail(sheet, template, headerIndex, taskFile.getId(), date, allContractNo, contractNoColumnName, columnFormats, yearT);					
				}
				
				if(result.rowNum == -1) {
					LOG.debug("Remove taskFile because Saving TaskDetail Error.");
					template.remove(taskFile);
					throw new CustomerException(4001, "Cann't save taskdetail.");
				}
				
				//---------: Table Index
				LOG.debug("Check and create Index.");
				template.indexOps(NEW_TASK_DETAIL.getName()).ensureIndex(new Index().on(SYS_FILE_ID.getName(), Direction.ASC));
				template.indexOps(NEW_TASK_DETAIL.getName()).ensureIndex(new Index().on(SYS_IS_ACTIVE.getName(), Direction.ASC));
				template.indexOps(NEW_TASK_DETAIL.getName()).ensureIndex(new Index().on(SYS_OLD_ORDER.getName(), Direction.ASC));
				template.indexOps(NEW_TASK_DETAIL.getName()).ensureIndex(new Index().on(SYS_TAGS.getName(), Direction.ASC));
				template.indexOps(NEW_TASK_DETAIL.getName()).ensureIndex(new Index().on(SYS_TAGS_U.getName(), Direction.ASC));
				template.indexOps(NEW_TASK_DETAIL.getName()).ensureIndex(new Index().on(SYS_APPOINT_DATE.getName(), Direction.ASC));
				template.indexOps(NEW_TASK_DETAIL.getName()).ensureIndex(new Index().on(SYS_NEXT_TIME_DATE.getName(), Direction.ASC));
				template.indexOps(NEW_TASK_DETAIL.getName()).ensureIndex(new Index().on(SYS_TRACE_DATE.getName(), Direction.ASC));
				
				//--: Set datatype
				if(result.dataTypes != null) {
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
				}
				
				//--: update rowNum to TaskFile.
				taskFile.setRowNum(result.rowNum);
				taskFile.setInsertRowNum(result.insertRowNum);
				taskFile.setUpdateRowNum(result.updateRowNum);
				template.save(taskFile);
				
				LOG.debug("Update Product setting");
				if(groupDatas != null) {
					product.setGroupDatas(groupDatas);					
				}
				product.setColumnFormats(columnFormats);
				templateCenter.save(product);
				
				//--: Save to disk for download purpose.
				LOG.debug("Start Thread saving file");
				
				new SaveFileService(workbook, path, fd.fileName).start();
			}
			
			return null;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private GeneralModel1 saveTaskDetailFirstTime(Sheet sheetAt, MongoTemplate template, Map<String, Integer> headerIndex, 
												String taskFileId, Date date, List<Map> allContractNumber, String contractNoColumnName) {
		GeneralModel1 result = new GeneralModel1();
		
		try {
			LOG.debug("Start save taskDetail");
			boolean isAllContractNumberEmpty = CollectionUtils.isEmpty(allContractNumber);
			List<Map<String, Object>> insertDatas = new ArrayList<>();
			Map<String, String> dataTypes = new HashMap<>();
			Date dummyDate = new Date(Long.MAX_VALUE);
			Set<String> keySet = headerIndex.keySet();
			Map<String, Object> data;
			Criteria updateCriteria;
			Set<String> updateKey;
			boolean isDup = false;
			int updateRowNum = 0;
			boolean isLastRow;
			Update update;
			Map dataDummy;
			String dtt;
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
				isDup = false;
				
				for (String key : keySet) {
					cell = row.getCell(headerIndex.get(key), MissingCellPolicy.RETURN_BLANK_AS_NULL);
					
					if(cell != null) {
						
						if(!isAllContractNumberEmpty && key.equals(contractNoColumnName)) {
							dataDummy = new HashMap();
							dataDummy.put(key, StringUtil.removeWhitespace(cell.getStringCellValue()));
							isDup = allContractNumber.contains(dataDummy);
						}
						
						switch(cell.getCellType()) {
						case Cell.CELL_TYPE_STRING: {
							data.put(key, StringUtil.removeWhitespace(cell.getStringCellValue())); 
							dtt = "str";
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
				
				if(isDup) {
					updateCriteria = Criteria.where(contractNoColumnName).is(data.get(contractNoColumnName));
					update = new Update();
					updateKey = data.keySet();
					update.set(SYS_FILE_ID.getName(), taskFileId);
					update.set(SYS_UPDATED_DATE_TIME.getName(), date);
					update.set(SYS_IS_ACTIVE.getName(), new IsActive(true, ""));
					
					for (String key : updateKey) {
						update.set(key, data.get(key));
					}
					
					template.updateFirst(Query.query(updateCriteria), update, NEW_TASK_DETAIL.getName());
					updateRowNum++;
				} else {					
					//--: Add row
					data.put(SYS_FILE_ID.getName(), taskFileId);
					data.put(SYS_OLD_ORDER.getName(), r);
					data.put(SYS_IS_ACTIVE.getName(), new IsActive(true, ""));
					data.put(SYS_CREATED_DATE_TIME.getName(), date);
					data.put(SYS_UPDATED_DATE_TIME.getName(), date);
					data.put(SYS_APPOINT_DATE.getName(), dummyDate);
					data.put(SYS_NEXT_TIME_DATE.getName(), dummyDate);
					data.put(SYS_TRACE_DATE.getName(), dummyDate);
					data.put(SYS_TAGS.getName(), new ArrayList<Tag>());
					data.put(SYS_TAGS_U.getName(), new ArrayList<Tag>());
					
					insertDatas.add(data);
				}
				
				r++;
			}
			
			template.insert(insertDatas, NEW_TASK_DETAIL.getName());
			
			result.rowNum = r;
			result.insertRowNum = insertDatas.size();
			result.updateRowNum = updateRowNum;
			result.dataTypes = dataTypes;
			
			LOG.info("rowNum: " + result.rowNum +", inserRowNum: " + result.insertRowNum +", updateRowNum: " + result.updateRowNum);
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			result.rowNum = -1;
			return result;
		}
	}
	
	private GeneralModel1 saveTaskDetail(Sheet sheetAt, MongoTemplate template, Map<String, Integer> headerIndex, 
			String taskFileId, Date date, List<Map> allContractNumber, String contractNoColumnName, List<ColumnFormat> columnFormats, List<YearType> yearType) {
		GeneralModel1 result = new GeneralModel1();
		
		try {
			LOG.debug("Start save taskDetail");
			boolean isAllContractNumberEmpty = CollectionUtils.isEmpty(allContractNumber);
			List<Map<String, Object>> insertDatas = new ArrayList<>();
			Date dummyDate = new Date(Long.MAX_VALUE);
			Map<String, Object> data;
			Criteria updateCriteria;
			Set<String> updateKey;
			boolean isDup = false;
			int updateRowNum = 0;
			boolean isLastRow;
			Update update;
			Map dataDummy;
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
				isDup = false;
				
				for (ColumnFormat colForm : columnFormats) {
					
					if(!headerIndex.containsKey(colForm.getColumnName())) continue;
					
					cell = row.getCell(headerIndex.get(colForm.getColumnName()), MissingCellPolicy.RETURN_BLANK_AS_NULL);
					
					if(cell != null) {
						if(!isAllContractNumberEmpty && colForm.getColumnName().equals(contractNoColumnName)) {
							dataDummy = new HashMap();
							dataDummy.put(colForm.getColumnName(), ExcelUtil.getValue(cell, "str", null, null));
							isDup = allContractNumber.contains(dataDummy);
						}
						
						data.put(colForm.getColumnName(), ExcelUtil.getValue(cell, colForm.getDataType(), yearType, colForm.getColumnName()));
						
						isLastRow = false;
					} else {
						data.put(colForm.getColumnName(), null);
					}
				}			
				
				//--: Break
				if(isLastRow) {
					r--;
					break;
				}
				
				if(isDup) {
					updateCriteria = Criteria.where(contractNoColumnName).is(data.get(contractNoColumnName));
					update = new Update();
					updateKey = data.keySet();
					update.set(SYS_FILE_ID.getName(), taskFileId);
					update.set(SYS_UPDATED_DATE_TIME.getName(), date);
					update.set(SYS_IS_ACTIVE.getName(), new IsActive(true, ""));
					
					for (String key : updateKey) {
						update.set(key, data.get(key));
					}
					
					template.updateFirst(Query.query(updateCriteria), update, NEW_TASK_DETAIL.getName());
					updateRowNum++;
				} else {					
					//--: Add row
					data.put(SYS_FILE_ID.getName(), taskFileId);
					data.put(SYS_OLD_ORDER.getName(), r);
					data.put(SYS_IS_ACTIVE.getName(), new IsActive(true, ""));
					data.put(SYS_CREATED_DATE_TIME.getName(), date);
					data.put(SYS_UPDATED_DATE_TIME.getName(), date);
					data.put(SYS_APPOINT_DATE.getName(), dummyDate);
					data.put(SYS_NEXT_TIME_DATE.getName(), dummyDate);
					data.put(SYS_TRACE_DATE.getName(), dummyDate);
					data.put(SYS_TAGS.getName(), new ArrayList<Tag>());
					data.put(SYS_TAGS_U.getName(), new ArrayList<Tag>());
					
					insertDatas.add(data);
				}
				r++;
			}
			
			if(insertDatas.size() > 0) {
				template.insert(insertDatas, NEW_TASK_DETAIL.getName());				
			}
			
			result.rowNum = r;
			result.insertRowNum = insertDatas.size();
			result.updateRowNum = updateRowNum;
			
			LOG.info("rowNum: " + result.rowNum +", inserRowNum: " + result.insertRowNum +", updateRowNum: " + result.updateRowNum);
			
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
			
			//---: Query Data
//			LOG.debug("Find product");
//			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(currentProduct)), Product.class);
			
//			List<String> contractNoVals = new ArrayList<>();
//			List<String> idCardVals = new ArrayList<>();
//			ProductSetting productSetting = product.getProductSetting();
//			String contractNoColumn = productSetting.getContractNoColumnName();
//			String contractNoColumnPayment = productSetting.getContractNoColumnNamePayment();
//			String idCardColumn = productSetting.getIdCardNoColumnName();
			
			Query query = Query.query(Criteria.where(SYS_FILE_ID.getName()).is(id));
//			query.fields().include(contractNoColumn).include(idCardColumn);
			
			//---: Query Data
//			LOG.debug("Find newTaskDetail");
//			List<Map> tasks = template.find(query, Map.class, NEW_TASK_DETAIL.getName());
			
			/*for (Map map : tasks) {
				contractNoVals.add(map.get(contractNoColumn).toString());
				idCardVals.add(map.get(idCardColumn).toString());
			}*/
			
			//---------: Remove others data
//			LOG.debug("Remove allRelated");
//			RemoveRelatedDataUtil.allRelated(template, contractNoVals, idCardVals, contractNoColumnPayment);
			
			//---------: Remove Task
			LOG.debug("Remove newTaskDetail");
			template.remove(query, NEW_TASK_DETAIL.getName());
			
			//---------: Remove file
			LOG.debug("Remove newTaskFile");
			NewTaskFile taskFile = template.findOne(Query.query(Criteria.where("id").is(id)), NewTaskFile.class);
			template.remove(taskFile);
			if(!new File(taskFile.getFilePath() + "/" + taskFile.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + taskFile.getFileName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Map<String, String> getTaskFile(NewTaskCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			NewTaskFile file = template.findOne(Query.query(Criteria.where("id").is(req.getId())), NewTaskFile.class);
			String filePath = file.getFilePath() + "/" + file.getFileName();
			
			Map<String, String> map = new HashMap<>();
			map.put("filePath", filePath);
			map.put("fileName", file.getFileName());
			
			return  map;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Map<String, String> getFirstTaskFile(String productId) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			Query query = Query.query(Criteria.where("enabled").is(true));
			query.with(new Sort(Sort.Direction.DESC, "createdDateTime"));
			
			ExportTemplateFile file = template.findOne(query, ExportTemplateFile.class);			
			String filePath = filePathExportTemplate + "/" + file.getFileName();
			
			Map<String, String> map = new HashMap<>();
			map.put("filePath", filePath);
			map.put("fileName", file.getFileName());
			
			return  map;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateEnabled(NewTaskUpdateCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			NewTaskFile noticeFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), NewTaskFile.class);
			
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
	
	public ExportTemplateFindCriteriaResp findExportTemplate(TraceResultReportFindCriteriaReq req) throws Exception {
		try {
			ExportTemplateFindCriteriaResp resp = new ExportTemplateFindCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = new Criteria();
			
			if(req.getEnabled() != null) {
				criteria.and("enabled").is(req.getEnabled());
			}
			
			Query query = Query.query(criteria);
			
			long totalItems = template.count(query, ExportTemplateFile.class);
			
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			
			List<ExportTemplateFile> files = template.find(query, ExportTemplateFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void saveExportTemplate(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String currentProduct) throws Exception {		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			
			LOG.debug("Get Filename");
			FileDetail fd = FileUtil.getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			
			Users user = ContextDetailUtil.getCurrentUser(templateCenter);
			MongoTemplate template = dbFactory.getTemplates().get(currentProduct);
			
			LOG.debug("Save new TaskFile");
			ExportTemplateFile exportTemplateFileFile = new ExportTemplateFile(fd.fileName, date);
			exportTemplateFileFile.setCreatedBy(user.getId());
			exportTemplateFileFile.setUpdateedDateTime(date);
			exportTemplateFileFile.setEnabled(true);
			template.insert(exportTemplateFileFile);
			
			File file = new File(filePathExportTemplate);
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder SUCCESS!");
			}
			
			Files.copy(uploadedInputStream, Paths.get(filePathExportTemplate + "/" + fd.fileName));
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateEnabledExportTemplate(TraceResultReportFindCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			ExportTemplateFile exportTemplateFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), ExportTemplateFile.class);
			
			if(exportTemplateFile.getEnabled()) {
				exportTemplateFile.setEnabled(false);
			} else {
				exportTemplateFile.setEnabled(true);
			}
			
			template.save(exportTemplateFile);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteFileExportTemplate(String productId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			ExportTemplateFile exportTemplateFile = template.findOne(Query.query(Criteria.where("id").is(id)), ExportTemplateFile.class);
			template.remove(exportTemplateFile);
			
			if(!new File(filePathExportTemplate + "/" + exportTemplateFile.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + exportTemplateFile.getFileName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Map<String, String> getFileExportTemplate(TraceResultReportFindCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Criteria criteria;
			
			if(req.getId() != null) {
				criteria = Criteria.where("id").is(req.getId());
			} else {
				criteria = Criteria.where("enabled").is(true);
			}
			
			ExportTemplateFile exportTemplateFile = template.findOne(Query.query(criteria), ExportTemplateFile.class);
			
			String filePath = filePathExportTemplate + "/" + exportTemplateFile.getFileName();
			
			Map<String, String> map = new HashMap<>();
			map.put("filePath", filePath);
			map.put("fileName", exportTemplateFile.getFileName());
			
			return  map;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateTemplateNameExportTemplate(TraceResultRportUpdateCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			ExportTemplateFile exportTemplateFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), ExportTemplateFile.class);
			exportTemplateFile.setTemplateName(req.getTemplateName());
			
			template.save(exportTemplateFile);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private List<Map> getAllContactNo(MongoTemplate template, String contractNoColumnName) {
		try {
			Query query = new Query();
			query.fields().include(contractNoColumnName).exclude("_id");
			
			return template.find(query, Map.class, NEW_TASK_DETAIL.getName());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
