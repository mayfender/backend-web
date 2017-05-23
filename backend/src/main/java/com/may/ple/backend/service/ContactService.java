package com.may.ple.backend.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SentMailCriteriaReq;
import com.may.ple.backend.utils.EmailUtil;

@Service
public class ContactService {
	private static final Logger LOG = Logger.getLogger(ContactService.class.getName());
	private SettingService settingService;
	
	@Autowired
	public ContactService(SettingService settingService) {
		this.settingService = settingService;
	}
	
	public void sentMail(SentMailCriteriaReq req) throws Exception {
		try {			
			String companyInfo = settingService.getClientInfo();
			
			StringBuilder msg = new StringBuilder();
			msg.append("-----------: Company Info :----------\n");
			msg.append(companyInfo);
			msg.append("-----------: Company Info :----------\n\n");
			msg.append("ชื่อ : " + StringUtils.stripToEmpty(req.getName()) + "\n");
			msg.append("เบอร์ติดต่อกลับ : " + StringUtils.stripToEmpty(req.getMobile()) + "\n");
			msg.append("ไอดีไลน์ : " + StringUtils.stripToEmpty(req.getLine()) + "\n");
			msg.append("อีเมล์ติดต่อกลับ : " + StringUtils.stripToEmpty(req.getEmail()) + "\n");
			msg.append("รายละเอียด : " + StringUtils.stripToEmpty(req.getDetail()) + "\n");
			
			EmailUtil.sendSimple("User_Client_Info", msg.toString());	
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}	
		
}
