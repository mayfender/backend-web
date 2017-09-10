package com.may.ple.backend.service;

import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OLD_ORDER;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.bussiness.ImportExcel;
import com.may.ple.backend.criteria.ImportOthersFindCriteriaReq;
import com.may.ple.backend.criteria.ImportOthersFindCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.GroupData;
import com.may.ple.backend.entity.ImportMenu;
import com.may.ple.backend.entity.ImportOthersFile;
import com.may.ple.backend.entity.ImportOthersSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.model.GeneralModel1;
import com.may.ple.backend.model.YearType;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.ExcelUtil;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;
import com.may.ple.backend.utils.POIExcelUtil;
import com.may.ple.backend.utils.StringUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class ImportOthersService {
	private static final Logger LOG = Logger.getLogger(ImportOthersService.class.getName());
	private static final int INIT_GROUP_ID = 1;
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	@Value("${file.path.task_others}")
	private String filePathTask;
	
	@Autowired
	public ImportOthersService(DbFactory dbFactory, MongoTemplate templateCenter) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
	}
	
	public ImportOthersFindCriteriaResp find(ImportOthersFindCriteriaReq req) throws Exception {
		try {
			ImportOthersFindCriteriaResp resp = new ImportOthersFindCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Query query = Query.query(Criteria.where("menuId").is(req.getMenuId()));
			
			long totalItems = template.count(query, ImportOthersFile.class);
			
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
				 .with(new Sort(Direction.DESC, "createdDateTime"));
			
			query.fields()
			.include("fileName")
			.include("createdDateTime")
			.include("rowNum");
			
			List<ImportOthersFile> files = template.find(query, ImportOthersFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	@SuppressWarnings("resource")
	public Map<String, Object> save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String productId, 
										String menuId, Boolean isConfirmImport, List<YearType> yearT) throws Exception {
		Workbook workbook = null;
		MongoTemplate template = null;
		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			List<GroupData> groupDatas = null;
			
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
			
			LOG.debug("Get db connection");
			template = dbFactory.getTemplates().get(productId);
			
			LOG.debug("Get importmenu");
			ImportMenu menu = template.findOne(Query.query(Criteria.where("id").is(menuId)), ImportMenu.class);
			List<ColumnFormat> columnFormats = menu.getColumnFormats();
			ImportOthersSetting setting = menu.getSetting();
			String contractNoColumnName = setting.getContractNoColumnName();
			String idCardNoColumnName = setting.getIdCardNoColumnName();
			String columnKey = StringUtils.isBlank(contractNoColumnName) ? idCardNoColumnName : contractNoColumnName;
			boolean isFirstTime = false;
			
			if(columnFormats == null) {
				if(!template.collectionExists(menuId)) {
					LOG.debug("Create collection " + menuId);
					template.createCollection(menuId);					
				}
				
				DBCollection collection = template.getCollection(menuId);
				collection.createIndex(new BasicDBObject(SYS_FILE_ID.getName(), 1));
				collection.createIndex(new BasicDBObject(SYS_OLD_ORDER.getName(), 1));
				
				columnFormats = new ArrayList<>();
			}
			
			Map<String, Integer> headerIndex;
			
			if(columnFormats.size() == 0) {
				GroupData groupData = new GroupData();
				groupData.setId(INIT_GROUP_ID);
				groupData.setName(menu.getMenuName());
				
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
				
				String path = filePathTask + "/" + FileUtil.getPath(productId);
				LOG.debug(path);
				
				LOG.debug("Save new OthersFile");
				ImportOthersFile othersFile = new ImportOthersFile(fd.fileName, date);
				othersFile.setMenuId(menuId);
				othersFile.setCreatedBy(user.getId());
				othersFile.setFilePath(path);
				template.insert(othersFile);
				
				GeneralModel1 result = null;
				
				if(isFirstTime) {
					LOG.debug("Save Othersfile Details for fistTime");
					result = saveOtherFileDetailFirstTime(sheet, template, headerIndex, othersFile.getId(), menuId);					
				} else {
					LOG.debug("Save Othersfile Details");
					result = saveOtherFileDetail(sheet, template, headerIndex, othersFile.getId(), menuId, columnFormats, yearT, columnKey);	
				}
				
				if(result.rowNum == -1) {
					LOG.debug("Remove taskFile because Saving TaskDetail Error.");
					template.remove(othersFile);
					throw new CustomerException(4001, "Cann't save taskdetail.");
				}
				
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
				othersFile.setRowNum(result.rowNum);
				template.save(othersFile);
				
				LOG.debug("Update ImportMenu setting");
				if(groupDatas != null) {
					menu.setGroupDatas(groupDatas);					
				}
				menu.setColumnFormats(columnFormats);
				template.save(menu);
				
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
	
	private GeneralModel1 saveOtherFileDetailFirstTime(Sheet sheetAt, MongoTemplate template, Map<String, Integer> headerIndex, String taskFileId, String menuId) {
		GeneralModel1 result = new GeneralModel1();
		
		try {
			LOG.debug("Start save saveOtherFileDetail");
			Date date = Calendar.getInstance().getTime();
			Set<String> keySet = headerIndex.keySet();
			List<Map<String, Object>> datas = new ArrayList<>();
			Map<String, String> dataTypes = new HashMap<>();
			Map<String, Object> data;
			Row row;
			int r = 1; //--: Start with row 1 for skip header row.
			Cell cell;
			boolean isLastRow;
			String dtt;
			
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
				
				//--: Add row
				data.put(SYS_FILE_ID.getName(), taskFileId);
				data.put(SYS_OLD_ORDER.getName(), r);
				data.put(SYS_CREATED_DATE_TIME.getName(), date);
				data.put(SYS_UPDATED_DATE_TIME.getName(), date);
				datas.add(data);
				r++;
			}
			
			template.insert(datas, menuId);
			result.rowNum = r;
			result.dataTypes = dataTypes;
			
			LOG.debug("End");
			return result;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			result.rowNum = -1;
			return result;
		}
	}
	
	private GeneralModel1 saveOtherFileDetail(Sheet sheetAt, MongoTemplate template, Map<String, Integer> headerIndex, 
												String taskFileId, String menuId, List<ColumnFormat> columnFormats, List<YearType> yearType, String columnKey) {
		GeneralModel1 result = new GeneralModel1();
		
		try {
			LOG.debug("Start save saveOtherFileDetail");
			Date date = Calendar.getInstance().getTime();
			List<Map<String, Object>> datas = new ArrayList<>();
			Map<String, Object> data;
			Row row;
			int r = 1; //--: Start with row 1 for skip header row.
			Cell cell;
			boolean isLastRow;
			
			row: while(true) {
				row = sheetAt.getRow(r);
				if(row == null) {
					r--;
					break;
				}
				
				data = new LinkedHashMap<>();
				isLastRow = true;
				
				for (ColumnFormat colForm : columnFormats) {
					if(!headerIndex.containsKey(colForm.getColumnName())) continue;
					
					cell = row.getCell(headerIndex.get(colForm.getColumnName()), MissingCellPolicy.RETURN_BLANK_AS_NULL);
					
					if(cell == null || StringUtils.isBlank(String.valueOf(cell))) {
						if(colForm.getColumnName().equals(columnKey)) {
							r++;
							continue row;
						}
						
						data.put(colForm.getColumnName(), null);
					} else {
						data.put(colForm.getColumnName(), ExcelUtil.getValue(cell, colForm.getDataType(), yearType, colForm.getColumnName()));
						isLastRow = false;
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
				data.put(SYS_CREATED_DATE_TIME.getName(), date);
				data.put(SYS_UPDATED_DATE_TIME.getName(), date);
				datas.add(data);
				r++;
			}
			
			if(datas.size() > 0) {
				template.insert(datas, menuId);				
			}
			result.rowNum = datas.size();
			
			LOG.debug("End");
			return result;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			result.rowNum = -1;
			return result;
		}
	}
	
	public void delete(String productId, String id, String menuId) throws Exception {
		try {
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			ImportOthersFile importOthersFile = template.findOne(Query.query(Criteria.where("id").is(id)), ImportOthersFile.class);
			template.remove(importOthersFile);
			template.remove(Query.query(Criteria.where(SYS_FILE_ID.getName()).is(id)), menuId);
			
			if(!new File(importOthersFile.getFilePath() + "/" + importOthersFile.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + importOthersFile.getFileName());
			}
			
			/*Query query = Query.query(Criteria.where("menuId").is(menuId));
			long taskNum = template.count(query, ImportOthersFile.class);
			
			if(taskNum == 0) {
				LOG.debug("Task is empty so remove ColumnFormats also");
				ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(menuId)), ImportMenu.class);
				importMenu.setColumnFormats(null);
				importMenu.setGroupDatas(null);
				template.save(importMenu);
				
				//--
				template.indexOps(menuId).dropAllIndexes();
			}*/
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
