package com.may.ple.backend.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.EngTplCriteriaReq;
import com.may.ple.backend.criteria.EngTplCriteriaResp;
import com.may.ple.backend.entity.EngineTpl;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.FileUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class EngTplService {
	private static final Logger LOG = Logger.getLogger(EngTplService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	@Value("${file.path.based}")
	private String filePathBased;
	
	@Autowired	
	public EngTplService(MongoTemplate templateCore, DbFactory dbFactory) {
		this.templateCore = templateCore;
		this.dbFactory = dbFactory;
	}
	
	public EngTplCriteriaResp getTpl(int currentPage, int itemsPerPage, int type, String prodId, Boolean enabled) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(prodId);
			Criteria criteria = Criteria.where("type").is(type);
			if(enabled != null) {
				criteria.and("enabled").is(enabled);
			}
			
			Query query = Query.query(criteria);
			long totalItems = template.count(query, EngineTpl.class);
			query.with(new PageRequest(currentPage - 1, itemsPerPage));
			query.with(new Sort("tplName"));
			List<EngineTpl> files = template.find(query, EngineTpl.class);			
			
			EngTplCriteriaResp resp = new EngTplCriteriaResp();
			resp.setFiles(files);
			resp.setTotalItems(totalItems);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String prodId, int type) throws Exception {		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			
			LOG.debug("Get Filename");
			FileDetail fd = FileUtil.getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			MongoTemplate template = dbFactory.getTemplates().get(prodId);
			
			EngineTpl engTpl = new EngineTpl(fd.fileName, date);
			engTpl.setCreatedBy(user.getId());
			engTpl.setUpdateedDateTime(date);
			engTpl.setType(type);
			engTpl.setEnabled(true);
			template.insert(engTpl);
			
			DBCollection collection = template.getCollection("engineTpl");
			collection.createIndex(new BasicDBObject("tplName", 1));
			
			File file = new File(getPath(type, prodId));
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.info("Create Folder SUCCESS!");
			}
			
			Files.copy(uploadedInputStream, Paths.get(file.getCanonicalPath() + "/" + fd.fileName));
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateTemplateName(EngTplCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			EngineTpl engineTpl = template.findOne(Query.query(Criteria.where("id").is(req.getId())), EngineTpl.class);
			engineTpl.setTplName(req.getTplName());
			template.save(engineTpl);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void delete(String prodId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(prodId);
			EngineTpl engineTpl = template.findOne(Query.query(Criteria.where("id").is(id)), EngineTpl.class);
			template.remove(engineTpl);
			
			if(!new File(getPath(engineTpl.getType(), prodId) + engineTpl.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + engineTpl.getFileName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateEnabled(EngTplCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			EngineTpl engineTpl = template.findOne(Query.query(Criteria.where("id").is(req.getId())), EngineTpl.class);
			
			if(engineTpl.getEnabled()) {
				engineTpl.setEnabled(false);
			} else {
				engineTpl.setEnabled(true);
			}
			
			template.save(engineTpl);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String getFile(EngTplCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = Criteria.where("id").is(req.getId());
			EngineTpl engineTpl = template.findOne(Query.query(criteria), EngineTpl.class);
			String filePath = getPath(engineTpl.getType(), req.getProductId()) + engineTpl.getFileName();
			
			return  filePath;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private String getPath(int type, String prodId) {
		return filePathBased + "/upload/engTpl/" + type + "/" + prodId + "/";
	}
	
}
