package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
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
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.criteria.PaymentFindCriteriaReq;
import com.may.ple.backend.criteria.TraceResultImportFindCriteriaReq;
import com.may.ple.backend.criteria.TraceResultImportFindCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.DymList;
import com.may.ple.backend.entity.DymListDet;
import com.may.ple.backend.entity.PaymentFile;
import com.may.ple.backend.entity.Product;
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
import com.may.ple.backend.utils.MappingUtil;
import com.may.ple.backend.utils.StringUtil;

@Service
public class TraceResultImportService {
	private static final Logger LOG = Logger.getLogger(TraceResultImportService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	@Value("${file.path.payment}")
	private String filePathPayment;
	private UserAction userAct;
	
	@Autowired
	public TraceResultImportService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.userAct = userAct;
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
			
			List<TraceResultImportFile> files = template.find(query, TraceResultImportFile.class);			
			
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
			GeneralModel1 saveResult = saveDetail(sheet, template, headerIndex, file.getId(), currentProduct);
			
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
			
			TraceResultImportFile file = template.findOne(Query.query(Criteria.where("id").is(id)), TraceResultImportFile.class);
			template.remove(file);
			template.remove(Query.query(Criteria.where("fileId").is(id)), TraceWork.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private GeneralModel1 saveDetail(Sheet sheetAt, MongoTemplate template, Map<String, Integer> headerIndex, String fileId, String productId) {
		GeneralModel1 result = new GeneralModel1();
		
		try {
			LOG.debug("Start save taskDetail");
			Set<String> keySet = headerIndex.keySet();
			
			List<Map> traceWorks = new ArrayList<>();
			Map traceWork;
			
			Map<String, List<DymListDet>> dymList = getDymList(template);
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			String contractNoColumnName = product.getProductSetting().getContractNoColumnName();
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			columnFormats = getColumnFormatsActive(columnFormats);
			
			List<Users> users = userAct.getUserByProductToAssign(productId).getUsers();
			
			List<Map<String, String>> userList;
			List<DymListDet> dymLstDets;
			List<String> ownerId;
			boolean isLastRow;
			String cellVal;
			Map taskDetail;
			Field fields;
			Map userMap;
			Query query;
			int r = 1; //--: Start with row 1 for skip header row.
			Cell cell;
			Row row;
			
			while(true) {
				row = sheetAt.getRow(r);
				if(row == null) {
					r--;
					break;
				}
				
				traceWork = new HashMap<>();
				isLastRow = true;
				
				for (String key : keySet) {
					cell = row.getCell(headerIndex.get(key), MissingCellPolicy.RETURN_BLANK_AS_NULL);
					
					if(cell != null) {
						if(key.equals("contractNo") || key.equals("resultText") || key.equals("tel")) {
							cellVal = StringUtil.removeWhitespace(new DataFormatter().formatCellValue(cell));
							traceWork.put(key, cellVal);
							
							if(key.equals("contractNo")) {
								query = Query.query(Criteria.where(contractNoColumnName).is(cellVal));
								fields = query.fields().include(SYS_OWNER_ID.getName());
								
								for (ColumnFormat colForm : columnFormats) {
									fields.include(colForm.getColumnName());
								}
								
								taskDetail = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
								if(taskDetail != null) {
									ownerId = (List)taskDetail.get(SYS_OWNER_ID.getName());
									userList = MappingUtil.matchUserId(users, ownerId.get(0));
									
									if(userList != null && userList.size() > 0) {
										userMap = (Map)userList.get(0);
										taskDetail.put(SYS_OWNER.getName(), userMap.get("showname"));
										traceWork.put("taskDetail", taskDetail);
										traceWork.put("createdBy", userMap.get("id"));
										traceWork.put("createdByName", userMap.get("showname"));
									}
								}
							}
						} else if(key.equals("createdDateTime") || key.equals("nextTimeDate") || key.equals("appointDate")) {
							traceWork.put(key, cell.getDateCellValue());
						} else if(key.equals("appointAmount")) {
							traceWork.put(key, cell.getNumericCellValue());
						} else if(key.endsWith("_sys")) {
							key = key.substring(0, key.indexOf("_sys"));
							
							if(dymList.containsKey(key)) {		
								cellVal = StringUtil.removeWhitespace(new DataFormatter().formatCellValue(cell));
								dymLstDets = dymList.get(key);
								
								for (DymListDet det : dymLstDets) {
									if((!StringUtils.isBlank(det.getCode()) && det.getCode().equals(cellVal)) || 
											(!StringUtils.isBlank(det.getMeaning()) && det.getMeaning().equals(cellVal))) {
										
										traceWork.put(key, new ObjectId(det.getId()));
										break;
									}
								}
							}
						}
						
						isLastRow = false;
					} else {
						traceWork.put(key, null);
					}
				}			
				
				//--: Break
				if(isLastRow) {
					r--;
					break;
				}
				
				//--: Save
				traceWork.put("fileId", fileId);
				traceWork.put("isHold", false);
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
	
	private Map<String, List<DymListDet>> getDymList(MongoTemplate template) {
		List<DymList> find = template.find(new Query(), DymList.class);
		Map<String, List<DymListDet>> dymLst = new HashMap<>();
		List<DymListDet> dymLstDets;
		
		for (DymList dymList : find) {
			dymLstDets = template.find(Query.query(Criteria.where("listId").is(new ObjectId(dymList.getId()))), DymListDet.class);
			dymLst.put(dymList.getFieldName(), dymLstDets);
		}
		
		return dymLst;
	}
	
	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats) {
		if(columnFormats == null) return null;
		
		List<ColumnFormat> result = new ArrayList<>();
		
		for (ColumnFormat colFormat : columnFormats) {
			if(colFormat.getIsActive()) {
				result.add(colFormat);
			}
		}
		
		return result;
	}
	
}
