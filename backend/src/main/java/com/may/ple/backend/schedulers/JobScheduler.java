package com.may.ple.backend.schedulers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.may.ple.backend.schedulers.jobs.Job;
import com.may.ple.backend.service.SettingService;
import com.may.ple.backend.utils.EmailUtil;

@Component
public class JobScheduler {
	private static final Logger LOG = Logger.getLogger(JobScheduler.class.getName());
	public List<Job> everyDayNoonHalf = new ArrayList<>(); 
	@Autowired
	private SettingService settingService;
	
	@Scheduled(cron="0 30 12 * * *")
    public void everyDayNoonHalf() {
		try {
	    	LOG.debug("Job size: " + everyDayNoonHalf.size());
	    	
	    	for (Job job : everyDayNoonHalf) {
				job.run();
			}
	    	
	    	LOG.debug("Send email");
	    	Map<String, String> data = settingService.getClientInfo();
			EmailUtil.sendSimple(data.get("comCode") + "_SystemSent", data.get("info"));	
	    	    	
	    	LOG.debug("The time is now " + String.format(Locale.ENGLISH, "%1$tH:%1$tM:%1$tS", Calendar.getInstance().getTime()));		
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
    }

}
