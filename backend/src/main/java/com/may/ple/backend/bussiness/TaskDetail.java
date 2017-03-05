package com.may.ple.backend.bussiness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.may.ple.backend.entity.ImportMenu;

public class TaskDetail {
	private static final Logger LOG = Logger.getLogger(TaskDetail.class.getName());
	
	public static List<String> getPgsIdNo(MongoTemplate template) {
		LOG.debug("Start");
		List<String> idCardLst = null;
		
		Query queryImportMenu = Query.query(Criteria.where("isPgs").is(true));
		queryImportMenu.fields().include("setting");
		
		ImportMenu importMenu = template.findOne(queryImportMenu, ImportMenu.class);
		LOG.debug("End find importMenu");
		
		if(importMenu == null) return idCardLst;
		
		String idCardNoColumnName = importMenu.getSetting().getIdCardNoColumnName();
		Query queryPgs = new Query();
		queryPgs.fields().include(idCardNoColumnName);
		
		LOG.debug("Find pgs");
		List<Map> pgsList = template.find(queryPgs, Map.class, importMenu.getId());
		idCardLst = new ArrayList<>();
		LOG.debug("End pgs");
		
		LOG.debug("Start Prepare idNo");
		for (Map map : pgsList) {
			idCardLst.add(map.get(idCardNoColumnName).toString());
		}
		LOG.debug("End Prepare idNo");
		
		return idCardLst;
	}

}
