package com.may.ple.backend.schedulers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.may.ple.backend.schedulers.jobs.APIUploadJobImpl;
import com.may.ple.backend.schedulers.jobs.Job;
import com.may.ple.backend.service.JWebsocketService;

@Component
public class JobScheduler {
	private static final Logger LOG = Logger.getLogger(JobScheduler.class.getName());
	public List<Job> everyDay12And20Half = new ArrayList<>();
	@Autowired
	private JWebsocketService jWService;
	@Autowired
	private APIUploadJobImpl apiJobImpl;

	@Scheduled(cron="0 30 12 * * *")
    public void everyDay12And20Half() {
		try {
	    	LOG.info("Job size: " + everyDay12And20Half.size());

	    	for (Job job : everyDay12And20Half) {
				job.run();
			}

	    	LOG.debug("The time is now " + String.format(Locale.ENGLISH, "%1$tH:%1$tM:%1$tS", Calendar.getInstance().getTime()));
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
    }

	@Scheduled(cron="0 0/1 * * * *")
    public void cron2() {
		try {
			LOG.info("Start cron2");
			//---:
//			apiJobImpl.proceed();

			//---:
			jWService.pushAlert();
			LOG.info("End cron2");
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
    }

}
