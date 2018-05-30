package com.may.ple.backend.service;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.ibm.icu.util.Calendar;
import com.may.ple.backend.criteria.SettingSaveCriteriaReq;
import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.EmailUtil;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.LogUtil;
import com.may.ple.backend.utils.NetworkInfoUtil;

@Service
public class SettingService {
	private static final Logger LOG = Logger.getLogger(SettingService.class.getName());
	private MongoTemplate template;
	private String chkPayIP;
	
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
			
			if(req.getBackupPath() != null) {
				List<String> backupPaths = new LinkedList<String>(Arrays.asList(req.getBackupPath().split(",")));
				appSetting.setBackupPath(backupPaths.get(0));
				
				if(backupPaths.size() > 1) {
					backupPaths.remove(0);
					appSetting.setBackupPathSpares(backupPaths);					
				} else {
					appSetting.setBackupPathSpares(null);
				}
			}
			
			appSetting.setBackupUsername(req.getBackupUsername());
			appSetting.setBackupPassword(req.getBackupPassword());
			appSetting.setPhoneWsServer(req.getPhoneWsServer());
			appSetting.setPhoneRealm(req.getPhoneRealm());
			appSetting.setPhoneDefaultPass(req.getPhoneDefaultPass());
			appSetting.setProductKey(req.getProductKey());
			appSetting.setPythonPath(req.getPythonPath());
			appSetting.setTesseractPath(req.getTesseractPath());
			appSetting.setWkhtmltopdfPath(req.getWkhtmltopdfPath());
			appSetting.setWebExtractIsEnabled(req.getWebExtractIsEnabled());
			appSetting.setSiteSpshUsername(req.getSiteSpshUsername());
			appSetting.setSiteSpshPassword(req.getSiteSpshPassword());
			appSetting.setSiteComptrollerUsername(req.getSiteComptrollerUsername());
			appSetting.setSiteComptrollerPassword(req.getSiteComptrollerPassword());
			appSetting.setSiteTrueTVUsername(req.getSiteTrueTVUsername());
			appSetting.setSiteTrueTVPassword(req.getSiteTrueTVPassword());
			
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
	
	public void openAndClose(SettingSaveCriteriaReq req) throws Exception {
		try {
			ApplicationSetting appSetting = template.findOne(new Query(), ApplicationSetting.class);
			
			if(appSetting == null) {
				appSetting = new ApplicationSetting();
			}
			
			appSetting.setIsDisable(req.getIsDisable());
			
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
	
	public void contactUs() throws Exception {
		try {
			Map<String, String> data = getClientInfo();
			
			StringBuilder msg = new StringBuilder();
			msg.append("-----------: Company Info :----------\n");
			msg.append(data.get("info"));
			msg.append("-----------: Company Info :----------\n\n");
			msg.append("------: Request to renew license :------");
			
			EmailUtil.sendSimple(data.get("comCode") + "_UserSent", msg.toString());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<String> getDBBackupDir() throws Exception {
		try {
			ApplicationSetting appSetting = template.findOne(new Query(), ApplicationSetting.class);
			String backupPath = appSetting.getBackupPath();
			
			List<String> dirList = new ArrayList<>();
			File[] files = FileUtil.listDir(backupPath);
			
			for (File dir : files) {
				dirList.add(dir.getName());
			}
			
			return dirList;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<FileDetail> getDBBackupFile(String path) throws Exception {
		try {
			ApplicationSetting appSetting = template.findOne(new Query(), ApplicationSetting.class);
			String backupPath = appSetting.getBackupPath();
			
			List<FileDetail> fileList = new ArrayList<>();
			File[] files = FileUtil.listFile(backupPath + File.separator + path);
			int underscoreIndex, dotIndex;
			FileDetail fileDetail;
			String fileDateStr;
        	Date fileDate;
			
			for (File file : files) {
				fileDetail = new FileDetail();
				
				underscoreIndex = file.getName().lastIndexOf("_");
				dotIndex = file.getName().lastIndexOf(".");
				fileDateStr = file.getName().substring(underscoreIndex + 1, dotIndex);
        		fileDate = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH).parse(fileDateStr);
				
				fileDetail.fileName = file.getName();
				fileDetail.fileSize = (file.length() / 1024) / 1024L; //--: Megabytes
				fileDetail.createdDateTime = fileDate;
				
				fileList.add(fileDetail);
			}
			
			return fileList;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<FileDetail> getBackupFile() throws Exception {
		try {
			ApplicationSetting appSetting = template.findOne(new Query(), ApplicationSetting.class);
			String backupPath = appSetting.getBackupPath();
			
			List<FileDetail> fileList = new ArrayList<>();
			File[] files = FileUtil.listFile(backupPath);
			int underscoreIndex, dotIndex;
			FileDetail fileDetail;
			String fileDateStr;
        	Date fileDate;
			
			for (File file : files) {
				fileDetail = new FileDetail();
				
				underscoreIndex = file.getName().lastIndexOf("_");
				dotIndex = file.getName().lastIndexOf(".");
				fileDateStr = file.getName().substring(underscoreIndex + 1, dotIndex);
        		fileDate = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH).parse(fileDateStr);
				
				fileDetail.fileName = file.getName();
				fileDetail.fileSize = (file.length() / 1024) / 1024L; //--: Megabytes
				fileDetail.createdDateTime = fileDate;
				
				fileList.add(fileDetail);
			}
			
			return fileList;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<FileDetail> getLogFile() throws Exception {
		try {
			String logFilePath = LogUtil.getLogFilePath();
			
			if(StringUtils.isBlank(logFilePath)) return null;
			
			LOG.info("Start get log file");
			File dir = new File(logFilePath);
			File[] files = dir.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.toLowerCase().endsWith(".log");
			    }
			});
			
			List<FileDetail> fileList = new ArrayList<>();
			FileDetail fileDetail;
			
			for (File file : files) {
				fileDetail = new FileDetail();
				fileDetail.fileName = file.getName();
				fileDetail.fileSize = (file.length() / 1024) / 1024L; //--: Megabytes
				fileList.add(fileDetail);
			}
			
			return fileList;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteDb(String filePath) throws Exception {
		try {
			FileUtils.forceDelete(new File(filePath));
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public String getChkPayIP() {
		return chkPayIP;
	}

	public void setChkPayIP(String chkPayIP) {
		this.chkPayIP = chkPayIP;
	}
	
}
