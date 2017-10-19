package com.may.ple.backend.service;

import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OLD_ORDER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_UPDATED_DATE_TIME;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.FileCommonCriteriaResp;
import com.may.ple.backend.criteria.PaymentFindCriteriaReq;
import com.may.ple.backend.criteria.PaymentOnlineChkCriteriaReq;
import com.may.ple.backend.entity.PaymentOnlineCheckFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.model.GeneralModel1;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.ExcelUtil;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;

@Service
public class PaymentOnlineCheckService {
	private static final Logger LOG = Logger.getLogger(PaymentOnlineCheckService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	
	@Autowired
	public PaymentOnlineCheckService(DbFactory dbFactory, MongoTemplate templateCenter) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
	}
	
	public FileCommonCriteriaResp find(PaymentFindCriteriaReq req) throws Exception {
		try {
			FileCommonCriteriaResp resp = new FileCommonCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = new Criteria();
			
			Query query = Query.query(criteria);
			
			long totalItems = template.count(query, PaymentOnlineCheckFile.class);
			
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage())).with(new Sort(Direction.DESC, "createdDateTime"));
			
			query.fields()
			.include("fileName")
			.include("createdDateTime")
			.include("rowNum");
			
			List<Map> files = template.find(query, Map.class, "paymentOnlineCheckFile");			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String productId) throws Exception {		
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
			
			LOG.debug("Get product");
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			ProductSetting setting = product.getProductSetting();
			
			Sheet sheet = workbook.getSheetAt(0);
			Users user = ContextDetailUtil.getCurrentUser(templateCenter);
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			LOG.debug("Save new file");
			PaymentOnlineCheckFile file = new PaymentOnlineCheckFile(fd.fileName, date);
			file.setCreatedBy(user.getId());
			file.setUpdateedDateTime(date);
			template.insert(file);
			
			LOG.debug("Get Header of excel file");
			Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet);
			
			GeneralModel1 saveResult = saveDetail(sheet, template, setting.getContractNoColumnName(), productId, headerIndex, file.getId(), date);								
			
			if(saveResult.rowNum == -1) {
				LOG.debug("Remove file because Saving error.");
				template.remove(file);
				throw new CustomerException(4001, "Cann't save.");
			}

			//--: update rowNum to TaskFile.
			file.setRowNum(saveResult.rowNum);
			template.save(file);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
			if(fileOut != null) fileOut.close();
		}
	}
	
	public void deleteFile(String productId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			PaymentOnlineCheckFile file = template.findOne(Query.query(Criteria.where("id").is(id)), PaymentOnlineCheckFile.class);
			template.remove(file);
			template.remove(Query.query(Criteria.where(SYS_FILE_ID.getName()).is(id)), "paymentOnlineChkDet");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void addContractNo(PaymentOnlineChkCriteriaReq req) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			
			Map<String, Object> data = new HashMap<>();
			data.put("contractNo", req.getContractNo());
			data.put("status", 1);
			data.put(SYS_CREATED_DATE_TIME.getName(), date);
			data.put(SYS_UPDATED_DATE_TIME.getName(), date);
			
			template.insert(data, "paymentOnlineChkDet");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private GeneralModel1 saveDetail(Sheet sheetAt, MongoTemplate template, String contNoColName, 
										String productId, Map<String, Integer> headerIndex, String fileId, Date date) {		
		GeneralModel1 result = new GeneralModel1();
		
		try {
			LOG.debug("Start save");
			String contractNoColName = "contractNo";
			List<Map<String, Object>> datas = new ArrayList<>();
			Integer contractNoIndex = headerIndex.get(contractNoColName);
			Map<String, Object> data;
			Row row;
			int r = 1; //--: Start with row 1 for skip header row.
			Cell cell;
			boolean isLastRow;
			
			while(true) {
				row = sheetAt.getRow(r);
				if(row == null) {
					r--;
					break;
				}
				
				data = new LinkedHashMap<>();
				isLastRow = true;
				
				cell = row.getCell(contractNoIndex, MissingCellPolicy.RETURN_BLANK_AS_NULL);
				
				if(cell != null) {
					data.put(contractNoColName, ExcelUtil.getValue(cell, "str", null, null));
					isLastRow = false;
				} else {
					data.put(contractNoColName, null);
				}
				
				//--: Break
				if(isLastRow) {
					r--;
					break;
				}
				
				//--: Add row
				data.put("status", 1);
				data.put(SYS_FILE_ID.getName(), fileId);
				data.put(SYS_CREATED_DATE_TIME.getName(), date);
				data.put(SYS_UPDATED_DATE_TIME.getName(), date);
				datas.add(data);
								
				r++;
			}
			
			if(datas.size() > 0) {
				template.insert(datas, "paymentOnlineChkDet");				
			}
			result.rowNum = datas.size();
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			result.rowNum = -1;
			return result;
		}
	}
	
}
