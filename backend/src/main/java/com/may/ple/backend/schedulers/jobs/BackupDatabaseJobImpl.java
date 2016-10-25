package com.may.ple.backend.schedulers.jobs;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.schedulers.JobScheduler;

@Component
public class BackupDatabaseJobImpl implements Job {
	private static final Logger LOG = Logger.getLogger(BackupDatabaseJobImpl.class.getName());
	private JobScheduler jobScheduler;
	
	@Autowired
	public BackupDatabaseJobImpl(JobScheduler jobScheduler) {
		this.jobScheduler = jobScheduler;
	}
	
	@PostConstruct
	public void register() {
		jobScheduler.everyDayOneAm.add(this);
		LOG.info("Regis this job");
	}
	
	@Override
	public void run() {
		new JobProcess().start();
	}
	
	class JobProcess extends Thread {
		
		@Override
		public void run() {
			LOG.info("Start");
			
			try {
				
				
			} catch (Exception e) {
				LOG.error(e.toString(), e);
			}
			LOG.info("End");
		}
	}
	
	public static void main(String[] args) {
		InputStream is = null;
		
		try {
			
            
            String line = "C:\\Program Files\\MongoDB\\Server\\3.2\\bin\\mongodump -uroot -p19042528 --out D:/test_dump_data ";
            CommandLine cmdLine = CommandLine.parse(line);
            DefaultExecutor executor = new DefaultExecutor();
            int exitValue = executor.execute(cmdLine);
            
			
			System.out.println("finished : " + exitValue);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if(is != null) is.close(); } catch (Exception e2) {}
		}
	}

}
