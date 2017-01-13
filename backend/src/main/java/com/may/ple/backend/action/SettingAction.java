package com.may.ple.backend.action;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.SettingDataCriteriaResp;
import com.may.ple.backend.criteria.SettingSaveCriteriaReq;
import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.schedulers.jobs.BackupDatabaseJobImpl;
import com.may.ple.backend.service.SettingService;

@Component
@Path("setting")
public class SettingAction {
	private static final Logger LOG = Logger.getLogger(SettingAction.class.getName());
	private SettingService service;
	private BackupDatabaseJobImpl backup;
	
	@Autowired
	public SettingAction(SettingService service, BackupDatabaseJobImpl backup) {
		this.service = service;
		this.backup = backup;
	}
	
	@GET
	@Path("/getData")
	@Produces(MediaType.APPLICATION_JSON)
	public SettingDataCriteriaResp getData() {
		LOG.debug("Start");
		SettingDataCriteriaResp resp = new SettingDataCriteriaResp();
		
		try {
			ApplicationSetting appSetting = service.getData();
			resp.setSetting(appSetting);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/update")
	public CommonCriteriaResp update(SettingSaveCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug(req);
			service.update(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateLicense")
	public CommonCriteriaResp updateLicense(SettingSaveCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug("Call updateLicense");
			service.updateLicense(req.getLicense(), req.getProductKey());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/forceBackup")
	public CommonCriteriaResp forceBackup() {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			
			LOG.debug("Call run backup");
			backup.runSync();
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}