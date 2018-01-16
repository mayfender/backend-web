package com.may.ple.backend.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.ColumnFormatDetActiveUpdateCriteriaReq;
import com.may.ple.backend.criteria.ColumnFormatDetUpdatreCriteriaReq;
import com.may.ple.backend.criteria.GetColumnFormatsCriteriaResp;
import com.may.ple.backend.criteria.GetColumnFormatsDetCriteriaResp;
import com.may.ple.backend.criteria.GroupDataUpdateCriteriaReq;
import com.may.ple.backend.criteria.ImportMenuDeleteCriteriaReq;
import com.may.ple.backend.criteria.ImportMenuFindCriteriaReq;
import com.may.ple.backend.criteria.ImportMenuSaveCriteriaReq;
import com.may.ple.backend.criteria.ImportOthersNoticeUpdateCriteriaReq;
import com.may.ple.backend.criteria.ImportOthersSettingCriteriaReq;
import com.may.ple.backend.criteria.ImportOthersUpdateColFormCriteriaReq;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.GroupData;
import com.may.ple.backend.entity.ImportMenu;
import com.may.ple.backend.entity.ImportOthersFile;
import com.may.ple.backend.entity.ImportOthersSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.ColumnFormatGroup;
import com.may.ple.backend.model.DbFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class ImportMenuService {
	private static final Logger LOG = Logger.getLogger(ImportMenuService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	private ProductService productService;
	@Value("${file.path.task_others}")
	private String filePathTask;
	
	@Autowired
	public ImportMenuService(DbFactory dbFactory, MongoTemplate templateCenter, ProductService productService) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.productService = productService;
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
			importMenu.setIsPgs(req.getIsPgs());
			importMenu.setUpdatedDateTime(date);
			
			if(importMenu.getSetting() == null) {
				importMenu.setSetting(new ImportOthersSetting());				
			}
			
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
			query.fields().include("menuName").include("isPgs");
			
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
	
	public GetColumnFormatsCriteriaResp getColumnFormat(String menuId, String productId) throws Exception {
		try {
			GetColumnFormatsCriteriaResp resp = new GetColumnFormatsCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(menuId)), ImportMenu.class);
			ImportOthersSetting setting = importMenu.getSetting();
			
			if(setting != null) {
				resp.setContractNoColumnName(setting.getContractNoColumnName());
				resp.setIdCardNoColumnName(setting.getIdCardNoColumnName());
			}
			
			List<ColumnFormat> mainColumnFormats = productService.getColumnFormat(productId);
			resp.setMainColumnFormats(mainColumnFormats);
			resp.setColumnFormats(importMenu.getColumnFormats());
			
			return resp;
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
				
				DBCollection collection = template.getCollection(req.getMenuId());
				if(req.getIsActive()) {
					collection.createIndex(new BasicDBObject(req.getColumnName(), 1));
				} else {
					collection.dropIndex(req.getColumnName() + "_1");
				}
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public GetColumnFormatsDetCriteriaResp getColumnFormatDet(String productId, String menuId) throws Exception {
		try {
			GetColumnFormatsDetCriteriaResp resp = new GetColumnFormatsDetCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(menuId)), ImportMenu.class);
			List<ColumnFormat> columnFormats = importMenu.getColumnFormats();
			List<GroupData> groupDatas = importMenu.getGroupDatas();
			
			if(columnFormats == null) return null;
			
			Map<Integer, List<ColumnFormat>> map = new HashMap<>();
			List<ColumnFormat> colFormLst;
			
			for (ColumnFormat colForm : columnFormats) {
				if(map.containsKey(colForm.getDetGroupId())) {					
					colFormLst = map.get(colForm.getDetGroupId());
					colFormLst.add(colForm);
				} else {
					colFormLst = new ArrayList<>();
					colFormLst.add(colForm);
					map.put(colForm.getDetGroupId(), colFormLst);
				}
			}
			
			resp.setGroupDatas(groupDatas);
			resp.setColFormMap(map);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateNotice(ImportOthersNoticeUpdateCriteriaReq req) throws Exception {
		try {
			LOG.debug("Start");
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(req.getMenuId())), ImportMenu.class);
			List<ColumnFormat> colForm = importMenu.getColumnFormats();
			
			for (ColumnFormat col : colForm) {		
				if(col.getColumnName().equals(req.getColumnName())) {
					if(col.getIsNotice() == null || !col.getIsNotice()) {
						col.setIsNotice(true);						
					} else {
						col.setIsNotice(false);
					}
					break;
				}				
			}
			
			importMenu.setUpdatedDateTime(new Date());
			template.save(importMenu);
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateGroupDatas(GroupDataUpdateCriteriaReq req) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(req.getMenuId())), ImportMenu.class);
			importMenu.setUpdatedDateTime(new Date());
			importMenu.setGroupDatas(req.getGroupDatas());
			
			template.save(importMenu);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateColumnFormatDet(ColumnFormatDetUpdatreCriteriaReq req) throws Exception {
		try {
			LOG.debug("Start");
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(req.getMenuId())), ImportMenu.class);
			List<ColumnFormat> colForm = importMenu.getColumnFormats();
			
			List<ColumnFormatGroup> colFormGroups = req.getColFormGroups();
			List<ColumnFormat> columnFormats;
			Integer groupId;
			
			for (ColumnFormatGroup columnFormatGroup : colFormGroups) {
				groupId = columnFormatGroup.getId();
				columnFormats = columnFormatGroup.getColumnFormats();
				
				for (ColumnFormat col : colForm) {		
					for (ColumnFormat columnFormat : columnFormats) {
						if(col.getColumnName().equals(columnFormat.getColumnName())) {
							col.setDetGroupId(groupId);
							col.setDetOrder(columnFormat.getDetOrder());
							break;
						}
					}
				}
			}
			
			importMenu.setUpdatedDateTime(new Date());
			template.save(importMenu);
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateColumnFormatDetActive(ColumnFormatDetActiveUpdateCriteriaReq req) throws Exception {
		try {
			LOG.debug("Start");
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(req.getMenuId())), ImportMenu.class);
			List<ColumnFormat> colForm = importMenu.getColumnFormats();
			ColumnFormat columnFormat;
			
			for (ColumnFormat col : colForm) {		
				columnFormat = req.getColumnFormat();
				
				if(col.getColumnName().equals(columnFormat.getColumnName())) {
					col.setDetIsActive(columnFormat.getDetIsActive());
					break;
				}				
			}
			
			importMenu.setUpdatedDateTime(new Date());
			template.save(importMenu);
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateColumnName(ImportOthersSettingCriteriaReq req) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(req.getMenuId())), ImportMenu.class);
			importMenu.setUpdatedDateTime(new Date());
			
			ImportOthersSetting setting = importMenu.getSetting();
			
			if(setting == null) {
				LOG.debug("Create new ProductSetting");
				setting = new ImportOthersSetting();
				importMenu.setSetting(setting);
			}
			
			if(!StringUtils.isBlank(req.getContractNoColumnName())) {
				setting.setContractNoColumnName(req.getContractNoColumnName());				
			} else if(!StringUtils.isBlank(req.getIdCardNoColumnName())) {
				setting.setIdCardNoColumnName(req.getIdCardNoColumnName());				
			}
			
			template.save(importMenu);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
