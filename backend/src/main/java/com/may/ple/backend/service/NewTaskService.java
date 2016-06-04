package com.may.ple.backend.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
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
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String currentProduct) throws Exception {
		Workbook workbook = null;
		
		try {
			
			if(fileDetail.getFileName().endsWith(".xlsx")) {
				workbook = new XSSFWorkbook(uploadedInputStream);	
			} else if(fileDetail.getFileName().endsWith(".xls")) {
				workbook = new HSSFWorkbook(uploadedInputStream);
			} else {
				throw new CustomerException(5000, "Filetype not match");
			}
			
			LOG.debug("Get product");
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(currentProduct)), Product.class);
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			
			if(columnFormats == null) columnFormats = new ArrayList<>();
			
			int cellIndex = 0;
			int countNull = 0;
			String value;
			Row row = workbook.getSheetAt(0).getRow(0);
			
			while(true) {
				Cell cell = row.getCell(cellIndex++, MissingCellPolicy.RETURN_BLANK_AS_NULL);				
				
				if(countNull == 10) break;
			
				if(cell == null) {
					countNull++;
					continue;
				}
				
				value = cell.getStringCellValue().trim();
				
				if(!columnFormats.contains(value)) {
					columnFormats.add(new ColumnFormat(cell.getStringCellValue().trim()));					
				}
			}
			
			product.setColumnFormats(columnFormats);
			templateCenter.save(product);
			
			LOG.debug("Get db connection by product and save new TaskFile");
			MongoTemplate template = dbFactory.getTemplates().get(currentProduct);
			template.insert(new NewTaskFile(new String (fileDetail.getFileName().getBytes ("iso-8859-1"), "UTF-8"), new Date()));
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
		}
	}
	
	public void deleteFileTask(String currentProduct, String id) throws Exception {
		try {
			
			MongoTemplate template = dbFactory.getTemplates().get(currentProduct);
			template.remove(Query.query(Criteria.where("id").is(id)), NewTaskFile.class);
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
