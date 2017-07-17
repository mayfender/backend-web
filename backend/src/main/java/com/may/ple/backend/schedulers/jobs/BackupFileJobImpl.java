package com.may.ple.backend.schedulers.jobs;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.may.ple.backend.schedulers.JobScheduler;
import com.may.ple.backend.utils.ZipUtil;

@Component
public class BackupFileJobImpl implements Job {
	private static final Logger LOG = Logger.getLogger(BackupFileJobImpl.class.getName());
	private JobScheduler jobScheduler;
	@Value("${file.path.notice}")
	private String filePathNotice;
	@Value("${file.path.traceResultReport}")
	private String filePathTraceResultReport;
	@Value("${file.path.exportTemplate}")
	private String filePathExportTemplate;
		
	public BackupFileJobImpl(){}
	
	@Autowired
	public BackupFileJobImpl(JobScheduler jobScheduler) {
		this.jobScheduler = jobScheduler;
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
	
	public static void main(String[] args) {
		System.out.println(new File("D:/test/test").getName());
	}
		
	class JobProcess extends Thread {
		
		@Override
		public void run() {			
			try {
					
				LOG.info("Start");
				
				Calendar car = Calendar.getInstance();
				
				File notice = new File(filePathNotice);
				File traceResultReport = new File(filePathTraceResultReport);
				File exportTemplate = new File(filePathExportTemplate);
				String dateTime = String.format(Locale.ENGLISH, "%1$tY%1$tm%1$td%1$tH%1$tM", car.getTime());
				
				LOG.info("Processing on " + filePathNotice);
				ZipUtil.createZip(filePathNotice, filePathNotice + "/"+ notice.getName() + "-bak_" + dateTime);
				
				LOG.info("Processing on " + filePathTraceResultReport);
				ZipUtil.createZip(filePathTraceResultReport, filePathTraceResultReport + "/"+ traceResultReport.getName() + "-bak_" + dateTime);
				
				LOG.info("Processing on " + filePathExportTemplate);
				ZipUtil.createZip(filePathExportTemplate, filePathExportTemplate + "/"+ exportTemplate.getName() + "-bak_" + dateTime);
				
	            LOG.info("End");
			} catch (Exception e) {
				LOG.error(e.toString(), e);
			}
		}
	}
	
}
