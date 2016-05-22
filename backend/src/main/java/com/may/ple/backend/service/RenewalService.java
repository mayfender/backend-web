package com.may.ple.backend.service;

import java.util.Date;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.may.ple.backend.constant.ExportTypeConstant;
import com.may.ple.backend.criteria.SptRegisteredFindCriteriaReq;
import com.may.ple.backend.entity.SptMemberRenewal;
import com.may.ple.backend.entity.SptMemberType;
import com.may.ple.backend.entity.SptReceipt;
import com.may.ple.backend.entity.SptRegistration;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.repository.SptMemberRenewalRepository;
import com.may.ple.backend.repository.SptMemberTypeRepository;
import com.may.ple.backend.repository.SptReceiptRepository;
import com.may.ple.backend.repository.SptRegistrationRepository;
import com.may.ple.backend.repository.UserRepository;

@Service
public class RenewalService {
	private static final Logger LOG = Logger.getLogger(RenewalService.class.getName());
	private SptRegistrationReceiptService sptRegistrationReceiptService;
	private SptMemberRenewalRepository sptMemberRenewalRepository;
	private SptRegistrationRepository sptRegistrationRepository;
	private SptMemberTypeRepository sptMemberTypeRepository;	
	private SptReceiptRepository sptReceiptRepository;
	private UserRepository userRepository;
	
	@Autowired
	public RenewalService(SptRegistrationRepository sptRegistrationRepository,
						  SptMemberRenewalRepository sptMemberRenewalRepository,
						  SptMemberTypeRepository sptMemberTypeRepository,
						  UserRepository userRepository,
						  SptReceiptRepository sptReceiptRepository,
						  SptRegistrationReceiptService sptRegistrationReceiptService) {
		this.sptRegistrationReceiptService = sptRegistrationReceiptService;
		this.sptMemberRenewalRepository = sptMemberRenewalRepository;
		this.sptRegistrationRepository = sptRegistrationRepository;
		this.sptMemberTypeRepository = sptMemberTypeRepository;
		this.sptReceiptRepository = sptReceiptRepository;
		this.userRepository = userRepository;
	}
	
	@Transactional
	public void renewal(SptRegisteredFindCriteriaReq req) {
		LOG.debug("Get registration");
		SptRegistration reg = sptRegistrationRepository.findOne(req.getRegId());
		
		LOG.debug("Save to Renewal table");
		Date date = new Date();
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Users createdBy = userRepository.findByUserName(user.getUsername());
		
		SptMemberType memberType = sptMemberTypeRepository.findOne(reg.getMemberTypeId());
		
		SptMemberRenewal renewal = new SptMemberRenewal(reg.getMemberId(), reg, 
								   reg.getRegisterDate(), reg.getExpireDate(), reg.getStatus(), 
								   true, reg.getPayType(), reg.getPrice() ,date, date, memberType, createdBy, createdBy);
		
		sptMemberRenewalRepository.save(renewal);
		
		LOG.debug("Save to SptReceipt table");
		String receiptNo = sptRegistrationReceiptService.genReceiptNo();
		SptReceipt sptReceipt = new SptReceipt(receiptNo, ExportTypeConstant.RECEIPT.getId(), date, date, reg.getRegId());
		sptReceiptRepository.save(sptReceipt);
		
		LOG.debug("Update registration");
		reg.setMemberTypeId(req.getMemberTypeId());
		reg.setRegisterDate(req.getRegisterDate());
		reg.setExpireDate(req.getExpireDate());
		reg.setStatus(0);
		
		LOG.debug("Save registration");
		sptRegistrationRepository.save(reg);
	}
	
	public void updateStatus(Long regId, Integer status) {
		SptRegistration registration = sptRegistrationRepository.findOne(regId);
		registration.setStatus(status);
		
		sptRegistrationRepository.save(registration);
	}
	
}
