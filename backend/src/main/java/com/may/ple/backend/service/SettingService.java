package com.may.ple.backend.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.ibm.icu.util.Calendar;
import com.may.ple.backend.criteria.SettingSaveCriteriaReq;
import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.utils.NetworkInfoUtil;

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
			Query query = new Query();
			query.fields().exclude("license");
			
			return template.findOne(query, ApplicationSetting.class);
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
			appSetting.setProductKey(req.getProductKey());
			
			if(!StringUtils.isBlank(req.getLicense())) {
				appSetting.setLicense(req.getLicense());				
			}
			
			LOG.debug("Save");
			template.save(appSetting);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateLicense(String license, String productKey) throws Exception {
		try {
			ApplicationSetting appSetting = template.findOne(new Query(), ApplicationSetting.class);
			
			if(appSetting == null) {
				appSetting = new ApplicationSetting();
			}
			
			if(!StringUtils.isBlank(productKey)) {				
				appSetting.setProductKey(productKey);
			}
			
			appSetting.setLicense(license);
			
			LOG.debug("Save");
			template.save(appSetting);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public ApplicationSetting getCer() throws Exception {
		try {
			Query query = new Query();
			query.fields().include("license").include("productKey");
			
			ApplicationSetting appSetting = template.findOne(query, ApplicationSetting.class);
			return appSetting;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Map<String, String> getClientInfo() throws Exception {
		try {
			Query query = new Query();
			query.fields()
			.include("productKey")
			.include("companyName");
			
			ApplicationSetting setting = template.findOne(query, ApplicationSetting.class);
			
			String myPubIp = NetworkInfoUtil.getPublicIp("http://api.ipify.org");					
			
			StringBuilder msg = new StringBuilder();
			msg.append("Company Name: " + setting.getCompanyName() + "\n");
			msg.append("Company Code: " + setting.getProductKey() + "\n");
			msg.append("IP ADDR: " + myPubIp + "\n");
			msg.append("Created: " + String.format(Locale.ENGLISH, "%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS", Calendar.getInstance().getTime()) + "\n");
			
			Map<String, String> resp = new HashMap<>();
			resp.put("info", msg.toString());
			resp.put("comCode", setting.getProductKey());
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
