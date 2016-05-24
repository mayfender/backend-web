package com.may.ple.backend.schedulers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(cron="0 0 1 * * *")
    public void everyDayOneAm() {
        System.out.println("The time is now " + dateFormat.format(new Date()));
    }

}
