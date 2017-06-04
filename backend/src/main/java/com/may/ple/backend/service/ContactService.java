package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.BankAccSaveCriteriaReq;
import com.may.ple.backend.criteria.SentMailCriteriaReq;
import com.may.ple.backend.entity.BankAccounts;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.EmailUtil;

@Service
public class ContactService {
	private static final Logger LOG = Logger.getLogger(ContactService.class.getName());
	private SettingService settingService;
	private MongoTemplate template;
	
	@Autowired
	public ContactService(SettingService settingService, MongoTemplate template) {
		this.settingService = settingService;
		this.template = template;
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
	
	public List<BankAccounts> findAccNo() throws Exception {
		try {			
			List<BankAccounts> bankAccs = template.find(new Query(), BankAccounts.class);
			return bankAccs;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String saveAccNo(BankAccSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(template);
			BankAccounts bankAccs;
			
			if(StringUtils.isBlank(req.getId())) {
				bankAccs = new BankAccounts(req.getAccNo());
				bankAccs.setCreatedDateTime(date);
				bankAccs.setUpdatedDateTime(date);
				bankAccs.setCreatedBy(user.getId());	
			} else {
				bankAccs = template.findOne(Query.query(Criteria.where("id").is(req.getId())), BankAccounts.class);
				bankAccs.setAccNo(req.getAccNo());
				bankAccs.setUpdatedDateTime(date);
				bankAccs.setUpdatedBy(user.getId());
			}
			
			LOG.debug("save");
			template.save(bankAccs);
			
			LOG.debug("sendBankAccData");
			sendBankAccData();
			
			return bankAccs.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteAccNo(String id) throws Exception {
		try {
			template.remove(Query.query(Criteria.where("id").is(id)), BankAccounts.class);
			
			LOG.debug("sendBankAccData");
			sendBankAccData();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void sendBankAccData() throws Exception {
		try {	
			String companyInfo = settingService.getClientInfo();
			List<BankAccounts> accNos = findAccNo();
			
			StringBuilder msg = new StringBuilder();
			msg.append("-----------: Company Info :----------\n");
			msg.append(companyInfo);
			msg.append("-----------: Company Info :----------\n\n");
			
			for (BankAccounts bankAccounts : accNos) {
				msg.append("เลขที่บัญชี : " + StringUtils.stripToEmpty(bankAccounts.getAccNo()) + "\n");				
			}
						
			EmailUtil.sendSimple("User_Client_Info", msg.toString());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
