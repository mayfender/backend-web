package com.may.ple.backend.schedulers.jobs;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.may.ple.backend.schedulers.JobScheduler;

@Component
public class SentEmailJobImpl implements Job {
	private static final Logger LOG = Logger.getLogger(SentEmailJobImpl.class.getName());
	private JobScheduler jobScheduler;
	private JavaMailSender mailSender;
	
	@Autowired
	public SentEmailJobImpl(JobScheduler jobScheduler, JavaMailSender mailSender) {
		this.jobScheduler = jobScheduler;
		this.mailSender = mailSender;
	}
	
	@PostConstruct
	public void register() {
		jobScheduler.everyDayOneAm.add(this);
		LOG.debug("Regis this job");
	}
	
	@Override
	public void run() {
		new JobProcess().start();
	}
	
	class JobProcess extends Thread {
		
		@Override
		public void run() {
			LOG.debug("Start Sent email.");
			
			try {
				
				MimeMessage mail = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(mail, true);
		        helper.setTo("mayfender@gmail.com");
		        //helper.setReplyTo("someone@localhost");
//		        helper.setFrom("mayfender.work@gmail.com");
		        helper.setSubject("Lorem ipsum");
		        helper.setText("Lorem ipsum dolor sit amet [...]");
		        
		        mailSender.send(mail);
				
			} catch (Exception e) {
				LOG.error(e.toString(), e);
			}
			
			LOG.debug("End Sent email.");
		}
		
	}

}
