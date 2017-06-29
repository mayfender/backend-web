package com.may.ple.backend.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import net.nicholaswilliams.java.licensing.exception.ExpiredLicenseException;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.DBBackupFindCriteriaReq;
import com.may.ple.backend.criteria.DBBackupFindCriteriaResp;
import com.may.ple.backend.criteria.SettingDataCriteriaResp;
import com.may.ple.backend.criteria.SettingSaveCriteriaReq;
import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.schedulers.jobs.BackupDatabaseJobImpl;
import com.may.ple.backend.service.SettingService;

@Component
@Path("setting")
public class SettingAction {
	private static final Logger LOG = Logger.getLogger(SettingAction.class.getName());
	private SettingService service;
	private BackupDatabaseJobImpl backup;
	private MongoTemplate template;
	
	@Autowired
	public SettingAction(SettingService service, BackupDatabaseJobImpl backup, MongoTemplate template) {
		this.service = service;
		this.backup = backup;
		this.template = template;
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
			
		} catch (ExpiredLicenseException e) {			
			resp.setStatusCode(6000);
			LOG.error(e.toString(), e);
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
	
	@GET
	@Path("/contactUs")
	public CommonCriteriaResp contactUs() {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			
			LOG.debug("Call run backup");
			service.contactUs();
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/findDBBackup")
	public CommonCriteriaResp findDBBackup(DBBackupFindCriteriaReq req) {
		LOG.debug("Start");
		DBBackupFindCriteriaResp resp = new DBBackupFindCriteriaResp();
		
		try {
			
			List<FileDetail> fileList = null;
			List<String> dirList = null;
			
			if(req.getIsInit() != null && req.getIsInit()) {
				LOG.debug("Call findDBBackupRoot");
				dirList = service.getDBBackupDir();
			}
			
			if((dirList != null && dirList.size() > 0) || !StringUtils.isBlank(req.getDir())) {				
				fileList = service.getDBBackupFile(StringUtils.isBlank(req.getDir()) ? dirList.get(0) : req.getDir());
				
				Collections.sort(fileList, new Comparator<FileDetail>() {
	                @Override
	                public int compare(FileDetail lhs, FileDetail rhs) {
	                	return rhs.createdDateTime.compareTo(lhs.createdDateTime);
	                }
	            });
			}
			
			resp.setDirList(dirList);
			resp.setFileList(fileList);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/downloadDBBack")
	public Response downloadDBBack(@QueryParam("dir") final String dir, final @QueryParam("fileName") String fileName) throws Exception {
		try {			
			StreamingOutput resp = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					OutputStream out = null;
					FileInputStream in = null;
					
					try {
						ApplicationSetting appSetting = template.findOne(new Query(), ApplicationSetting.class);
						String backupPath = appSetting.getBackupPath();
						String filePath = backupPath + File.separator + dir + File.separator + fileName;
						
						in = new FileInputStream(filePath);
						out = new BufferedOutputStream(os);
						int bytes;
						
						while ((bytes = in.read()) != -1) {
							out.write(bytes);
						}
						
						LOG.debug("End");
					} catch (Exception e) {
						LOG.error(e.toString());
					} finally {
						try { if(in != null) in.close(); } catch (Exception e2) {}			
						try { if(out != null) out.close(); } catch (Exception e2) {}			
					}	
				}
			};
			
				
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
}