package com.may.ple.backend.schedulers.jobs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.ProductSearchCriteriaReq;
import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.entity.Database;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.repository.ProductRepository;
import com.may.ple.backend.schedulers.JobScheduler;
import com.may.ple.backend.service.SettingService;
import com.may.ple.backend.utils.ExecUtil;
import com.may.ple.backend.utils.ZipUtil;

@Component
public class BackupDatabaseJobImpl implements Job {
	private static final Logger LOG = Logger.getLogger(BackupDatabaseJobImpl.class.getName());
	private JobScheduler jobScheduler;
	private SettingService settingService;
	private ProductRepository productRepository;
	
	@Autowired
	public BackupDatabaseJobImpl(JobScheduler jobScheduler, SettingService settingService, ProductRepository productRepository) {
		this.jobScheduler = jobScheduler;
		this.settingService = settingService;
		this.productRepository = productRepository;
	}
	
	@PostConstruct
	public void register() {
		jobScheduler.everyDayNoonHalf.add(this);
		LOG.info("Regis this job");
	}
	
	@Override
	public void run() {
		new JobProcess().start();
	}
	
	class JobProcess extends Thread {
		
		@Override
		public void run() {			
			try {
					
				LOG.info("Start");
				ApplicationSetting appSetting = settingService.getData();
				
				if(appSetting == null || 
						StringUtils.isBlank(appSetting.getMongdumpPath()) ||
						StringUtils.isBlank(appSetting.getBackupPath()) ||
						StringUtils.isBlank(appSetting.getBackupTime()) ||
						StringUtils.isBlank(appSetting.getBackupUsername()) ||
						StringUtils.isBlank(appSetting.getBackupPassword())) {
					return;
				}
				
				runEachProd(appSetting);
				
	            LOG.info("End");
			} catch (Exception e) {
				LOG.error(e.toString(), e);
			}
		}
		
		private void runEachProd(ApplicationSetting appSetting) throws Exception {
			try {
				
				Calendar car = Calendar.getInstance();
				ProductSearchCriteriaReq req = new ProductSearchCriteriaReq();
				req.setEnabled(1);
				req.setCurrentPage(1);
				req.setItemsPerPage(1000);
				
				List<Product> products = productRepository.findByEnabled(1);
				
				if(products == null) return;
				
				Database db;
				String dbName, host;
				Integer port;
				List<String> hostChk = new ArrayList<>();
				
				for (Product prod : products) {
					db = prod.getDatabase();
					
					if(db == null) continue;
					
					dbName = db.getDbName();
					host = db.getHost();
					port = db.getPort();
					
					if(StringUtils.isBlank(dbName) || StringUtils.isBlank(host) || port == null) continue;
					
					if(hostChk.contains(host)) continue;
					
					exec(appSetting, car, host, port);
					hostChk.add(host);
				}
				
			} catch (Exception e) {
				LOG.error(e.toString());
				throw e;
			}
		}
		
		private void exec(ApplicationSetting appSetting, Calendar car, String host, int port) {
			try {
				
				String backupRoot = appSetting.getBackupPath() + "/" + host;
				String backupDir = backupRoot + "/bak_" + String.format("%1$tY%1$tm%1$td%1$tH%1$tM", car.getTime());
				String command = "%s/mongodump --host %s --username %s --password %s --out %s";
				
				command = String.format(command, appSetting.getMongdumpPath(), host, appSetting.getBackupUsername(), appSetting.getBackupPassword(), backupDir);
	            String fileZip = backupDir + ".zip";
	            
	            LOG.debug("Call exec");
	            ExecUtil.exec(command, 0);
	            
	            LOG.debug("Call createZip");
	            ZipUtil.createZip(backupDir, fileZip);
	            
	            LOG.debug("Delete backup folder because just zip file need.");
	            FileUtils.deleteDirectory(new File(backupDir));
	            
	            List<File> files = (List<File>) FileUtils.listFiles(new File(backupRoot), new String[]{".zip"}, false);
	            car.add(Calendar.MONTH, -1);
	            
	            if(files != null) {
	            	int underscoreIndex, dotIndex;
	            	String fileDateStr;
	            	Date fileDate;
	            	
	            	for (File file : files) {
	            		underscoreIndex = file.getName().lastIndexOf("_");
	            		dotIndex = file.getName().lastIndexOf(".");
	            		fileDateStr = file.getName().substring(underscoreIndex, dotIndex);
	            		fileDate = new SimpleDateFormat("yyyyMMddhhmmss").parse(fileDateStr);
	            		
	            		if(fileDate.before(car.getTime())) {
	            			LOG.debug("fileDate: " + fileDate + ", (now - 1 month): " + car.getTime());
	            		}
					}
	            }
			} catch (Exception e) {
				LOG.error(e.toString());
			}
		}
	}
	
}
