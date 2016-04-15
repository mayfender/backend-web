package com.may.ple.backend.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SptRegisteredFindCriteriaReq;
import com.may.ple.backend.entity.SptRegistration;
import com.may.ple.backend.repository.SptRegistrationRepository;

@Service
public class RenewalService {
	private static final Logger LOG = Logger.getLogger(RenewalService.class.getName());
	private SptRegistrationRepository sptRegistrationRepository;
	
	@Autowired
	public RenewalService(SptRegistrationRepository sptRegistrationRepository) {
		this.sptRegistrationRepository = sptRegistrationRepository;
	}
	
	public void renewal(SptRegisteredFindCriteriaReq req) {
		SptRegistration registration = sptRegistrationRepository.findOne(req.getRegId());
		registration.setMemberTypeId(req.getMemberTypeId());
		registration.setExpireDate(req.getExpireDate());
		
		sptRegistrationRepository.save(registration);
	}
	
	public void updateStatus(Long regId, Integer status) {
		SptRegistration registration = sptRegistrationRepository.findOne(regId);
		registration.setStatus(status);
		
		sptRegistrationRepository.save(registration);
	}
	
}
