package com.may.ple.backend.schedulers.jobs;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.schedulers.JobScheduler;
import com.may.ple.backend.service.SettingService;
import com.may.ple.backend.utils.ZipUtil;

@Component
public class BackupFileJobImpl implements Job {
	private static final Logger LOG = Logger.getLogger(BackupFileJobImpl.class.getName());
	private SettingService settingService;
	private JobScheduler jobScheduler;
	@Value("${file.path.notice}")
	private String filePathNotice;
	@Value("${file.path.traceResultReport}")
	private String filePathTraceResultReport;
	@Value("${file.path.forecastResultReport}")
	private String filePathForecastResultReport;
	@Value("${file.path.exportTemplate}")
	private String filePathExportTemplate;
		
	public BackupFileJobImpl(){}
	
	@Autowired
	public BackupFileJobImpl(JobScheduler jobScheduler, SettingService settingService) {
		this.jobScheduler = jobScheduler;
		this.settingService = settingService;
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
				
				Calendar car = Calendar.getInstance();
				
				File notice = new File(filePathNotice);
				File traceResultReport = new File(filePathTraceResultReport);
				File forecastResultReport = new File(filePathForecastResultReport);
				File exportTemplate = new File(filePathExportTemplate);
				String dateTime = String.format(Locale.ENGLISH, "%1$tY%1$tm%1$td%1$tH%1$tM", car.getTime());
				
				LOG.info("Get ApplicationSetting");
				ApplicationSetting appSetting = settingService.getData();
				String backupRoot = appSetting.getBackupPath();
				String suffix = "-bak_" + dateTime + ".zip";
				
				LOG.info("Processing on " + filePathNotice);
				String path = backupRoot + "/" + notice.getName() + suffix;
				ZipUtil.createZip(filePathNotice, path);
				copy(appSetting, path);
				
				LOG.info("Processing on " + filePathTraceResultReport);
				path = backupRoot + "/" + traceResultReport.getName() + suffix;
				ZipUtil.createZip(filePathTraceResultReport, path);
				copy(appSetting, path);
				
				LOG.info("Processing on " + filePathForecastResultReport);
				path = backupRoot + "/" + forecastResultReport.getName() + suffix;
				ZipUtil.createZip(filePathForecastResultReport, path);
				copy(appSetting, path);
				
				LOG.info("Processing on " + filePathExportTemplate);
				path = backupRoot + "/" + exportTemplate.getName() + suffix;
				ZipUtil.createZip(filePathExportTemplate, path);
				copy(appSetting, path);
				
				LOG.debug("Call clearFile");
	            BackupCommons.clearFileOldThan1Month(backupRoot);
	            
	            LOG.info("End");
			} catch (Exception e) {
				LOG.error(e.toString(), e);
			}
		}
	}
	
	private void copy(ApplicationSetting appSetting, String srcPath) {
		try {
			if(appSetting.getBackupPathSpares() != null) {
	        	for (String path: appSetting.getBackupPathSpares()) {
	        		FileUtils.copyFile(new File(srcPath), new File(path + "/" + FilenameUtils.getName(srcPath)));
	        		LOG.info("Copy " + srcPath + " to " + path);
	        		
	        		LOG.debug("Call clearFile");
		            BackupCommons.clearFileOldThan1Month(path);
				}
	        }
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
	}
	
}
