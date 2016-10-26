package com.may.ple.backend.schedulers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.may.ple.backend.schedulers.jobs.Job;

@Component
public class JobScheduler {
	private static final Logger LOG = Logger.getLogger(JobScheduler.class.getName());
	public List<Job> everyDayNoonHalf = new ArrayList<>(); 
	
	@Scheduled(cron="0 30 12 * * *")
    public void everyDayOneAm() {
    	
    	LOG.debug("Job size: " + everyDayNoonHalf.size());
    	
    	for (Job job : everyDayNoonHalf) {
			job.run();
		}
    	    	
    	LOG.debug("The time is now " + String.format("%1$tH:%1$tM:%1$tS", Calendar.getInstance().getTime()));		
    	
    }

}
