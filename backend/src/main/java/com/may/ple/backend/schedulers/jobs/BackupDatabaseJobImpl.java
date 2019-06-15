package com.may.ple.backend.schedulers.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
	
	public BackupDatabaseJobImpl(){}
	
	@Autowired
	public BackupDatabaseJobImpl(JobScheduler jobScheduler, SettingService settingService, ProductRepository productRepository) {
		this.jobScheduler = jobScheduler;
		this.settingService = settingService;
		this.productRepository = productRepository;
	}
	
	@PostConstruct
	public void register() {
		jobScheduler.everyDay12And20Half.add(this);
		LOG.info("Regis this job");
	}
	
	@Override
	public void run() {
		new JobProcess().start();
	}
	
	@Override
	public void runSync() {
		new JobProcess().run();
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
				String host, hostPort;
				Integer port;
				List<String> hostPortChk = new ArrayList<>();
				
				for (Product prod : products) {
					db = prod.getDatabase();
					
					if(db == null) continue;
					
					host = db.getHost();
					port = db.getPort();
					
					if(StringUtils.isBlank(host) || port == null) continue;
					
					hostPort = host + "_" + port;
					
					if(hostPortChk.contains(hostPort)) continue;
					
					exec(appSetting, car, host, port);
					hostPortChk.add(hostPort);
				}
				
			} catch (Exception e) {
				LOG.error(e.toString());
				throw e;
			}
		}
		
		private void exec(ApplicationSetting appSetting, Calendar car, String host, int port) {
			try {
				
				String backupRoot = appSetting.getBackupPath() + "/" + host +"_" + port;
				String backupDir = backupRoot + "/db-bak_" + String.format(Locale.ENGLISH, "%1$tY%1$tm%1$td%1$tH%1$tM", car.getTime());
				String command = "%s/mongodump --host %s --port %s --username %s --password %s --out %s";
				
				command = String.format(command, appSetting.getMongdumpPath(), host, String.valueOf(port), appSetting.getBackupUsername(), appSetting.getBackupPassword(), backupDir);
	            String fileZip = backupDir + ".zip";
	            
	            LOG.debug("Call clearFile");
	            BackupCommons.clearFileOldThan1Month(backupRoot);
	            
	            LOG.debug("Call exec");
	            ExecUtil.exec(command, 0);
	            
	            LOG.debug("Call createZip");
	            ZipUtil.createZip(backupDir, fileZip);
	            
	            LOG.debug("Delete backup folder because just zip file need.");
	            FileUtils.deleteDirectory(new File(backupDir));
	            
	            if(appSetting.getBackupPathSpares() != null) {
	            	for (String path: appSetting.getBackupPathSpares()) {
	            		backupRoot = path + "/" + host +"_" + port;
	            		FileUtils.copyFile(new File(fileZip), new File(backupRoot + "/" + FilenameUtils.getName(fileZip)));
	            		LOG.info("Copy " + fileZip + " to " + path);
	            		
	            		LOG.debug("Call clearFile");
	    	            BackupCommons.clearFileOldThan1Month(backupRoot);
					}
	            }
			} catch (Exception e) {
				LOG.error(e.toString(), e);
			}
		}
	}
	
}
