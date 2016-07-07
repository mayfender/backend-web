package com.may.ple.backend.service;

import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OLD_ORDER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.ImportOthersFindDetailCriteriaReq;
import com.may.ple.backend.criteria.ImportOthersFindDetailCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.ImportMenu;
import com.may.ple.backend.model.DbFactory;

@Service
public class ImportOthersDetailService {
	private static final Logger LOG = Logger.getLogger(ImportOthersDetailService.class.getName());
	private DbFactory dbFactory;
	
	@Autowired
	public ImportOthersDetailService(DbFactory dbFactory) {
		this.dbFactory = dbFactory;
	}
	
	public ImportOthersFindDetailCriteriaResp find(ImportOthersFindDetailCriteriaReq req) throws Exception {
		try {
			ImportOthersFindDetailCriteriaResp resp = new ImportOthersFindDetailCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(req.getMenuId())), ImportMenu.class);
			List<ColumnFormat> columnFormats = importMenu.getColumnFormats();
			
			if(columnFormats == null) return resp;
			LOG.debug("Before size: " + columnFormats.size());
			columnFormats = getColumnFormatsActive(columnFormats);
			LOG.debug("After size: " + columnFormats.size());
			
			Criteria criteria = Criteria.where(SYS_FILE_ID.getName()).is(req.getFileId());
			Query query = Query.query(criteria);
			Field fields = query.fields();
			List<Criteria> multiOr = new ArrayList<>();
			
			for (ColumnFormat columnFormat : columnFormats) {
				fields.include(columnFormat.getColumnName());
				
				if(columnFormat.getDataType() != null) {
					if(columnFormat.getDataType().equals("str")) {
						if(!StringUtils.isBlank(req.getKeyword())) {								
							multiOr.add(Criteria.where(columnFormat.getColumnName()).regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));							
						}
					} else if(columnFormat.getDataType().equals("num")) {
						//--: Ignore right now.
					}
				} else {
					LOG.debug(columnFormat.getColumnName() + "' dataType is null");
				}
			}
			
			Criteria[] multiOrArr = multiOr.toArray(new Criteria[multiOr.size()]);
			if(multiOrArr.length > 0) {
				criteria.orOperator(multiOrArr);				
			}
			
			LOG.debug("Start Count newTaskDetail record");
			long totalItems = template.count(query, req.getMenuId());
			LOG.debug("End Count newTaskDetail record");
			
			query = query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			
			if(req.getColumnName() == null) {
				query.with(new Sort(SYS_OLD_ORDER.getName()));
			} else {				
				query.with(new Sort(Direction.fromString(req.getOrder()), req.getColumnName()));
			}
			
			LOG.debug("Start find newTaskDetail");
			List<Map> dataLst = template.find(query, Map.class, req.getMenuId());			
			LOG.debug("End find newTaskDetail");
			
			LOG.debug("Change id from ObjectId to normal ID");
			for (Map map : dataLst) {
				map.put("id", map.get("_id").toString()); 
				map.remove("_id");
			}
			
			resp.setHeaders(columnFormats);
			resp.setTotalItems(totalItems);
			resp.setDataLst(dataLst);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	
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
			Row row = sheet.getRow(0);
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
					columnFormats.add(new ColumnFormat(value, false));										
				}
			}
			
			return headerIndex;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	@SuppressWarnings("resource")
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String productId, String menuId) throws Exception {
		Workbook workbook = null;
		MongoTemplate template = null;
		
		try {
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
			
			LOG.debug("Get db connection");
			template = dbFactory.getTemplates().get(productId);
			
			LOG.debug("Get importmenu");
			ImportMenu menu = template.findOne(Query.query(Criteria.where("id").is(menuId)), ImportMenu.class);
			List<ColumnFormat> columnFormats = menu.getColumnFormats();
			
			if(columnFormats == null) {
				if(!template.collectionExists(menuId)) {
					LOG.debug("Create collection " + menuId);
					template.createCollection(menuId);					
				}
				
				template.indexOps(menuId).ensureIndex(new Index().on(SYS_FILE_ID.getName(), Direction.ASC));
				template.indexOps(menuId).ensureIndex(new Index().on(SYS_OLD_ORDER.getName(), Direction.ASC));
				columnFormats = new ArrayList<>();
			}
			
			LOG.debug("Get Header of excel file");
			Map<String, Integer> headerIndex = getFileHeader(sheet, columnFormats);
			
			if(headerIndex.size() > 0) {
				LOG.debug("Call getCurrentUser");
				Users user = ContextDetailUtil.getCurrentUser(templateCenter);
				
				LOG.debug("Save new OthersFile");
				ImportOthersFile othersFile = new ImportOthersFile(fd.fileName, date);
				othersFile.setMenuId(menuId);
				othersFile.setCreatedBy(user.getId());
				template.insert(othersFile);
				
				LOG.debug("Save Othersfile Details");
				GeneralModel1 result = saveOtherFileDetail(sheet, template, headerIndex, othersFile.getId(), menuId);
				
				if(result.rowNum == -1) {
					LOG.debug("Remove taskFile because Saving TaskDetail Error.");
					template.remove(othersFile);
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
				othersFile.setRowNum(result.rowNum);
				template.save(othersFile);
				
				LOG.debug("Update columnFormats of Product");
				menu.setColumnFormats(columnFormats);
				template.save(menu);
				
				//--: Save to disk for download purpose.
				LOG.debug("Start Thread saving file");
				new SaveFileService(workbook, filePathTask, fd.fileName).start();
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private GeneralModel1 saveOtherFileDetail(Sheet sheetAt, MongoTemplate template, Map<String, Integer> headerIndex, String taskFileId, String menuId) {
		GeneralModel1 result = new GeneralModel1();
		
		try {
			LOG.debug("Start save saveOtherFileDetail");
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
							data.put(key, cell.getStringCellValue()); 
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
	
	public void delete(String productId, String id, String menuId) throws Exception {
		try {
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			ImportOthersFile importOthersFile = template.findOne(Query.query(Criteria.where("id").is(id)), ImportOthersFile.class);
			template.remove(importOthersFile);
			template.remove(Query.query(Criteria.where(SYS_FILE_ID.getName()).is(id)), menuId);
			
			if(!new File(filePathTask + "/" + importOthersFile.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + importOthersFile.getFileName());
			}
			
			Query query = Query.query(Criteria.where("menuId").is(menuId));
			long taskNum = template.count(query, ImportOthersFile.class);
			
			if(taskNum == 0) {
				LOG.debug("Task is empty so remove ColumnFormats also");
				ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(menuId)), ImportMenu.class);
				importMenu.setColumnFormats(null);
				template.save(importMenu);
				
				//--
				template.indexOps(menuId).dropAllIndexes();
			}
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}*/
	
	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats) {
		List<ColumnFormat> result = new ArrayList<>();
		
		for (ColumnFormat colFormat : columnFormats) {
			if(colFormat.getIsActive()) {
				result.add(colFormat);
			}
		}
		
		return result;
	}
	
}