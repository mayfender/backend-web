package com.may.ple.backend.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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

import com.may.ple.backend.criteria.PaymentDetailCriteriaReq;
import com.may.ple.backend.criteria.PaymentReportFindCriteriaResp;
import com.may.ple.backend.criteria.TraceResultReportFindCriteriaReq;
import com.may.ple.backend.entity.PaymentReportFile;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.FileUtil;

@Service
public class PaymentReportService {
	private static final Logger LOG = Logger.getLogger(PaymentReportService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	@Value("${file.path.exportTemplate}")
	private String filePathExportTemplate;
	
	@Autowired
	public PaymentReportService(DbFactory dbFactory, MongoTemplate templateCenter) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
	}
	
	public PaymentReportFindCriteriaResp find(TraceResultReportFindCriteriaReq req) throws Exception {
		try {
			PaymentReportFindCriteriaResp resp = new PaymentReportFindCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = new Criteria();
			
			if(req.getEnabled() != null) {
				criteria.and("enabled").is(req.getEnabled());
			}
			
			Query query = Query.query(criteria);
			query.with(new Sort(Direction.DESC, "createdDateTime"));
			
			long totalItems = template.count(query, PaymentReportFile.class);
			
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			
			List<PaymentReportFile> files = template.find(query, PaymentReportFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String currentProduct) throws Exception {		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			
			LOG.debug("Get Filename");
			FileDetail fd = FileUtil.getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			
			Users user = ContextDetailUtil.getCurrentUser(templateCenter);
			MongoTemplate template = dbFactory.getTemplates().get(currentProduct);
			
			LOG.debug("Save new File");
			PaymentReportFile forecastResultReportFile = new PaymentReportFile(fd.fileName, date);
			forecastResultReportFile.setCreatedBy(user.getId());
			forecastResultReportFile.setUpdateedDateTime(date);
			forecastResultReportFile.setEnabled(true);
			template.insert(forecastResultReportFile);
			
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
	
	/*public void updateEnabled(TraceResultReportFindCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			ForecastResultReportFile forecastResultFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), ForecastResultReportFile.class);
			
			if(forecastResultFile.getEnabled()) {
				forecastResultFile.setEnabled(false);
			} else {
				forecastResultFile.setEnabled(true);
			}
			
			template.save(forecastResultFile);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}*/
	
	public Map<String, String> getFile(PaymentDetailCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Criteria criteria;
			
			if(req.getId() != null) {
				criteria = Criteria.where("id").is(req.getId());
			} else {
				criteria = Criteria.where("enabled").is(true);
			}
			
			Query query = Query.query(criteria);
			query.with(new Sort(Direction.DESC, "createdDateTime"));
			PaymentReportFile paymentReportFile = template.findOne(query, PaymentReportFile.class);
			
			String filePath = filePathExportTemplate + "/" + paymentReportFile.getFileName();
			
			Map<String, String> map = new HashMap<>();
			map.put("filePath", filePath);
			map.put("fileName", paymentReportFile.getFileName());
			
			return  map;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	
	public void deleteFile(String productId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			PaymentReportFile paymentReportFile = template.findOne(Query.query(Criteria.where("id").is(id)), PaymentReportFile.class);
			template.remove(paymentReportFile);
			
			if(!new File(filePathExportTemplate + "/" + paymentReportFile.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + paymentReportFile.getFileName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	/*
	public void updateTemplateName(TraceResultRportUpdateCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			ForecastResultReportFile forecastResultFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), ForecastResultReportFile.class);
			forecastResultFile.setTemplateName(req.getTemplateName());
			
			template.save(forecastResultFile);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}*/
	
}
