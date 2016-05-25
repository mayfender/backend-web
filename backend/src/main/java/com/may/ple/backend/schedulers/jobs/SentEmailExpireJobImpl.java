package com.may.ple.backend.schedulers.jobs;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
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
				
				LOG.debug("Notify 60 days advance.");
				List<SptRegistration> expireAdvance = sptRegistrationService.findExpireAdvance(60);
				String subj = "แจ้งต่ออายุสมาชิก";
				StringBuilder body = new StringBuilder();
				body.append("เรียน คุณxxx yyy\n\n");
				body.append("บริษัทฯ รู้สึกเป็นเกียรติ\n");
				body.append("และมีความยินดีอย่างยิ่งที่ได้รับความไว้วางใจจากท่านสมัครเป็นสมาชิกซุปเปอร์เทรดเดอร์ รีพับบลิค\n");
				body.append("ด้วยความตั้งใจของทีมงานที่จะสร้างให้เกิดเป็นพื้นที่ในการเรียนรู้การลงทุนอย่างถูกต้อง\n");
				body.append("สร้างผลตอบแทนจากการลงทุนที่เหมาะสมและยั่งยืน\n");
				body.append("ทั้งนี้บริษัทฯ ใคร่ขอแจ้งต่อท่านสมาชิกว่า อายุสมาชิกของท่านเหลือ zzz เดือน\n");
				body.append("โดยสิทธิสมาชิกของท่านจะสิ้นสุดในวันที่ ttt หากท่านมีความประสงค์ต้องการต่ออายุสมาชิก\n");
				body.append("สามารถติดต่อกับเจ้าหน้าที่ได้ที่ ซุปเปอร์เทรดเดอร์ รีพับบลิค อาคารเอ็มไพร์ทาวเวอร์ ชั้นที่ 19 ตึก 3\n\n");
				body.append("ด้วยความเคารพ\n\n");
				body.append("ทีมงานซุปเปอร์เทรดเดอร์ รีพับบลิค\n\n");
				
				String bodyStr;
				
				for (SptRegistration reg : expireAdvance) {
					bodyStr = body.toString()
							  .replaceFirst("xxx", reg.getFirstname())
						      .replaceFirst("yyy", reg.getLastname())
						      .replaceFirst("zzz", "2")
						      .replaceFirst("ttt", String.format("%1$td/%1$tm/%1$tY", reg.getExpireDate()));
					
					if(StringUtils.isBlank(reg.getConEmail())) continue;
					
					sendMail(reg.getConEmail(), subj, bodyStr);
				}
				
				//---------------------------------------------------------------------
				
				LOG.debug("Notify 30 days advance.");
				expireAdvance = sptRegistrationService.findExpireAdvance(30);
				
				for (SptRegistration reg : expireAdvance) {
					bodyStr = body.toString()
					  .replaceFirst("xxx", reg.getFirstname())
				      .replaceFirst("yyy", reg.getLastname())
				      .replaceFirst("zzz", "1")
				      .replaceFirst("ttt", String.format("%1$td/%1$tm/%1$tY", reg.getExpireDate()));
					
					if(StringUtils.isBlank(reg.getConEmail())) continue;
					
					sendMail(reg.getConEmail(), subj, bodyStr);				
				}
				
				//---------------------------------------------------------------------
				
				LOG.debug("today notify.");
				expireAdvance = sptRegistrationService.findExpireAdvance(0);
				subj = "แจ้งการหมดอายุสมาชิก";
				body = new StringBuilder();
				body.append("เรียน คุณxxx yyy\n\n");
				body.append("บริษัทฯ รู้สึกเป็นเกียรติ\n");
				body.append("และมีความยินดีอย่างยิ่งที่ได้รับความไว้วางใจจากท่านสมัครเป็นสมาชิกซุปเปอร์เทรดเดอร์ รีพับบลิค\n");
				body.append("ด้วยความตั้งใจของทีมงานที่จะสร้างให้เกิดเป็นพื้นที่ในการเรียนรู้การลงทุนอย่างถูกต้อง\n");
				body.append("สร้างผลตอบแทนจากการลงทุนที่เหมาะสมและยั่งยืน\n");
				body.append("ทั้งนี้บริษัทฯ ใคร่ขอแจ้งต่อท่านสมาชิกว่า สิทธิสมาชิกของท่านได้หมดแล้ว\n");
				body.append("หากท่านมีความประสงค์ต้องการต่ออายุสมาชิก สามารถติดต่อกับเจ้าหน้าที่ได้ที่\n");
				body.append("ซุปเปอร์เทรดเดอร์ รีพับบลิค อาคารเอ็มไพร์ทาวเวอร์ ชั้นที่ 19 ตึก 3\n\n");
				body.append("ด้วยความเคารพ\n\n");
				body.append("ทีมงานซุปเปอร์เทรดเดอร์ รีพับบลิค\n\n");
				
				for (SptRegistration reg : expireAdvance) {
					bodyStr = body.toString().replaceFirst("xxx", reg.getFirstname()).replaceFirst("yyy", reg.getLastname());
							
					if(StringUtils.isBlank(reg.getConEmail())) continue;
							
					sendMail(reg.getConEmail(), subj, bodyStr);	
				}
				
			} catch (Exception e) {
				LOG.error(e.toString(), e);
			}
			LOG.debug("End");
		}
		
		public void sendMail(String mailTo, String subj, String body) {
			try {
				
				MimeMessage mail = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(mail, true);
				helper.setTo(mailTo);
				helper.setSubject(subj);
				helper.setText(body);
				mailSender.send(mail);
				
			} catch (Exception e) {
				LOG.error(e.toString());
			}
		}
		
	}

}
