package com.may.ple.backend.service;

import java.util.Date;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SptRegisteredFindCriteriaReq;
import com.may.ple.backend.entity.SptMemberRenewal;
import com.may.ple.backend.entity.SptMemberType;
import com.may.ple.backend.entity.SptRegistration;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.repository.SptMemberRenewalRepository;
import com.may.ple.backend.repository.SptMemberTypeRepository;
import com.may.ple.backend.repository.SptRegistrationRepository;
import com.may.ple.backend.repository.UserRepository;

@Service
public class RenewalService {
	private static final Logger LOG = Logger.getLogger(RenewalService.class.getName());
	private SptRegistrationRepository sptRegistrationRepository;
	private SptMemberRenewalRepository sptMemberRenewalRepository;
	private SptMemberTypeRepository sptMemberTypeRepository;	
	private UserRepository userRepository;
	
	@Autowired
	public RenewalService(SptRegistrationRepository sptRegistrationRepository,
						  SptMemberRenewalRepository sptMemberRenewalRepository,
						  SptMemberTypeRepository sptMemberTypeRepository,
						  UserRepository userRepository) {
		this.sptRegistrationRepository = sptRegistrationRepository;
		this.sptMemberRenewalRepository = sptMemberRenewalRepository;
		this.sptMemberTypeRepository = sptMemberTypeRepository;
		this.userRepository = userRepository;
	}
	
	@Transactional
	public void renewal(SptRegisteredFindCriteriaReq req) {
		LOG.debug("Get registration");
		SptRegistration reg = sptRegistrationRepository.findOne(req.getRegId());
		
		//-------------------------------------------------
		Date date = new Date();
		
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Users createdBy = userRepository.findByUserName(user.getUsername());
		
		SptMemberType memberType = sptMemberTypeRepository.findOne(reg.getMemberTypeId());
		
		SptMemberRenewal renewal = new SptMemberRenewal(reg.getMemberId(), reg, 
								   reg.getRegisterDate(), reg.getExpireDate(), reg.getStatus(), 
								   true, reg.getPayType(), reg.getPrice() ,date, date, memberType, createdBy, createdBy);
		
		sptMemberRenewalRepository.save(renewal);
		//-------------------------------------------------
		
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
