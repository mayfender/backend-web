package com.may.ple.backend.schedulers.jobs;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.may.ple.backend.entity.SptRegistration;
import com.may.ple.backend.schedulers.JobScheduler;
import com.may.ple.backend.service.SptRegistrationService;

@Component
public class SentEmailExpireJobImpl implements Job {
	private static final Logger LOG = Logger.getLogger(SentEmailExpireJobImpl.class.getName());
	private JobScheduler jobScheduler;
	private JavaMailSender mailSender;
	private SptRegistrationService sptRegistrationService;
	
	@Autowired
	public SentEmailExpireJobImpl(JobScheduler jobScheduler, JavaMailSender mailSender,SptRegistrationService sptRegistrationService) {
		this.jobScheduler = jobScheduler;
		this.mailSender = mailSender;
		this.sptRegistrationService = sptRegistrationService;
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
			LOG.debug("Start");
			
			try {
				
				LOG.debug("Find 60 days advance.");
				List<SptRegistration> expireAdvance = sptRegistrationService.findExpireAdvance(60);
				MimeMessage mail;
				MimeMessageHelper helper;
				
				StringBuilder msg = new StringBuilder();
				msg.append("เรียน คุณxxx xxxx\n\n");
				msg.append("บริษัทฯ รู้สึกเป็นเกียรติ\n");
				msg.append("และมีความยินดีอย่างยิ่งที่ได้รับความไว้วางใจจากท่านสมัครเป็นสมาชิกซุปเปอร์เทรดเดอร์ รีพับบลิค\n");
				msg.append("ด้วยความตั้งใจของทีมงานที่จะสร้างให้เกิดเป็นพื้นที่ในการเรียนรู้การลงทุนอย่างถูก\n");
				msg.append("ต้อง สร้างผลตอบแทนจากการลงทุนที่เหมาะสมและยั่งยืน\n");
				msg.append("ทั้งนี้บริษัทฯ ใคร่ขอแจ้งต่อท่านสมาชิกว่า สิทธิสมาชิกของท่านได้หมดแล้ว\n");
				msg.append("หากท่านมีความประสงค์ต้องการต่ออายุสมาชิก สามารถติดต่อกับเจ้าหน้าที่ได้ที่\n");
				msg.append("ซุปเปอร์เทรดเดอร์ รีพับบลิค อาคารเอ็มไพร์ทาวเวอร์ ชั้นที่ 19 ตึก 3\n\n");
				msg.append("ด้วยความเคารพ\n\n");
				msg.append("ทีมงานซุปเปอร์เทรดเดอร์ รีพับบลิค\n\n");
				
				for (SptRegistration reg : expireAdvance) {
					mail = mailSender.createMimeMessage();
					helper = new MimeMessageHelper(mail, true);
					helper.setTo(reg.getConEmail());
					helper.setSubject("แจ้งการหมดอายุสมาชิก");
					helper.setText(msg.toString().replaceFirst("xxx", reg.getFirstname()).replaceFirst("xxxx", reg.getLastname()));
					
					mailSender.send(mail);
				}
				
				
				
				LOG.debug("Find 30 days advance.");
				expireAdvance = sptRegistrationService.findExpireAdvance(30);
				for (SptRegistration reg : expireAdvance) {
					LOG.debug(reg);					
				}
				
				LOG.debug("today days advance.");
				expireAdvance = sptRegistrationService.findExpireAdvance(0);
				for (SptRegistration reg : expireAdvance) {
					LOG.debug(reg);					
				}
				
				
				/*MimeMessage mail = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(mail, true);
		        helper.setTo("mayfender@gmail.com");*/
		        //helper.setReplyTo("someone@localhost");
//		        helper.setFrom("mayfender.work@gmail.com");
		        /*helper.setSubject("Lorem ipsum");
		        helper.setText("Lorem ipsum dolor sit amet [...]");
		        
		        mailSender.send(mail);*/
				
			} catch (Exception e) {
				LOG.error(e.toString(), e);
			}
			LOG.debug("End");
		}
		
	}

}
