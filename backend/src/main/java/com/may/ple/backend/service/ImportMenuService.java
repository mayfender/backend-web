package com.may.ple.backend.service;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.ImportMenuDeleteCriteriaReq;
import com.may.ple.backend.criteria.ImportMenuFindCriteriaReq;
import com.may.ple.backend.criteria.ImportMenuSaveCriteriaReq;
import com.may.ple.backend.criteria.ImportOthersUpdateColFormCriteriaReq;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.ImportMenu;
import com.may.ple.backend.entity.ImportOthersFile;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;

@Service
public class ImportMenuService {
	private static final Logger LOG = Logger.getLogger(ImportMenuService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	@Value("${file.path.task_others}")
	private String filePathTask;
	
	@Autowired
	public ImportMenuService(DbFactory dbFactory, MongoTemplate templateCenter) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
	}
	
	public String save(ImportMenuSaveCriteriaReq req) throws Exception {
		LOG.debug("Start");
		try {
			Date date = Calendar.getInstance().getTime();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());			
			
			LOG.debug("Get user from context");
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String username = authentication.getName();
			LOG.debug("User from context: " + username);
			
			LOG.debug("Get user id from database");
			Users user = templateCenter.findOne(Query.query(Criteria.where("username").is(username)), Users.class);
			LOG.debug("User id from database: " + user.getId());
			
			ImportMenu importMenu;
			
			if(req.getId() != null) {
				importMenu = template.findOne(Query.query(Criteria.where("id").is(req.getId())), ImportMenu.class);
				importMenu.setUpdatedDateTime(date);
				importMenu.setUpdatedBy(user.getId());
			} else {
				importMenu = new ImportMenu(req.getMenuName(), true);				
				importMenu.setCreatedDateTime(date);
				importMenu.setCreatedBy(user.getId());
			}
						
			importMenu.setMenuName(req.getMenuName());
			importMenu.setUpdatedDateTime(date);
			
			template.save(importMenu);
			LOG.debug("End");
			return importMenu.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<ImportMenu> find(ImportMenuFindCriteriaReq req) throws Exception {
		LOG.debug("Start");
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());			
			
			Query query = Query.query(Criteria.where("enabled").is(req.getEnabled()));
			query.fields().include("menuName");
			
			List<ImportMenu> menus = template.find(query, ImportMenu.class);
			
			LOG.debug("End");
			return menus;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void delete(ImportMenuDeleteCriteriaReq req) throws Exception {
		LOG.debug("Start");
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());			
			
			template.remove(Query.query(Criteria.where("id").is(req.getId())), ImportMenu.class);
						
			template.dropCollection(req.getId());
			
			List<ImportOthersFile> files = template.find(Query.query(Criteria.where("menuId").is(req.getId())), ImportOthersFile.class);
			
			for (ImportOthersFile importFile : files) {
				if(!new File(filePathTask + "/" + importFile.getFileName()).delete()) {
					LOG.warn("Cann't delete file " + importFile.getFileName());
				}				
			}
			
			template.remove(Query.query(Criteria.where("menuId").is(req.getId())), ImportOthersFile.class);
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<ColumnFormat> getColumnFormat(String menuId, String productId) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(menuId)), ImportMenu.class);
			return importMenu.getColumnFormats();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateColumnFormat(ImportOthersUpdateColFormCriteriaReq req) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(req.getMenuId())), ImportMenu.class);
			importMenu.setUpdatedDateTime(new Date());
			importMenu.setColumnFormats(req.getColumnFormats());
			
			template.save(importMenu);
			
			if(req.getIsActive() != null) {
				LOG.debug("Update index");
				
				if(req.getIsActive()) {
					template.indexOps(req.getMenuId()).ensureIndex(new Index().on(req.getColumnName(), Direction.ASC));										
				} else {
					template.indexOps(req.getMenuId()).dropIndex(req.getColumnName() + "_1");
				}
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
