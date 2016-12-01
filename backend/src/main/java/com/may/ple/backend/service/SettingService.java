package com.may.ple.backend.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SettingSaveCriteriaReq;
import com.may.ple.backend.entity.ApplicationSetting;

@Service
public class SettingService {
	private static final Logger LOG = Logger.getLogger(SettingService.class.getName());
	private MongoTemplate template;
	
	@Autowired	
	public SettingService(MongoTemplate template) {
		this.template = template;
	}
	
	public ApplicationSetting getData() throws Exception {
		try {			

			return template.findOne(new Query(), ApplicationSetting.class);			
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void update(SettingSaveCriteriaReq req) throws Exception {
		try {
			ApplicationSetting appSetting = template.findOne(new Query(), ApplicationSetting.class);
			
			if(appSetting == null) {
				appSetting = new ApplicationSetting();
			}
			
			appSetting.setCompanyName(req.getCompanyName());
			appSetting.setMongdumpPath(req.getMongdumpPath());
			appSetting.setBackupPath(req.getBackupPath());
			appSetting.setBackupUsername(req.getBackupUsername());
			appSetting.setBackupPassword(req.getBackupPassword());
			appSetting.setPhoneWsServer(req.getPhoneWsServer());
			appSetting.setPhoneRealm(req.getPhoneRealm());
			appSetting.setPhoneDefaultPass(req.getPhoneDefaultPass());
			
			LOG.debug("Save");
			template.save(appSetting);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
