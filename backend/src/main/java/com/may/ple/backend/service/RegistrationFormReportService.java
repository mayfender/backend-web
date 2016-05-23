package com.may.ple.backend.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SptRegistrationEditCriteriaResp;
import com.may.ple.backend.entity.SptMemberType;
import com.may.ple.backend.entity.SptRegistration;
import com.may.ple.backend.pdf.RegistrationForm;
import com.may.ple.backend.repository.SptMemberRenewalRepository;
import com.may.ple.backend.repository.SptMemberTypeRepository;

@Service
public class RegistrationFormReportService {
	private static final Logger LOG = Logger.getLogger(RegistrationFormReportService.class.getName());
	private SptMemberRenewalRepository sptMemberRenewalRepository;
	private SptMemberTypeRepository memberTypeRepository;
	private SptRegistrationService service;
	
	@Autowired
	public RegistrationFormReportService(SptMemberRenewalRepository sptMemberRenewalRepository, 
										 SptRegistrationService service, 
										 SptMemberTypeRepository memberTypeRepository) {
		this.sptMemberRenewalRepository = sptMemberRenewalRepository;
		this.service = service;
		this.memberTypeRepository = memberTypeRepository;
	}
	
	public byte[] proceed(Long id) throws Exception {
		try {
			
			SptRegistrationEditCriteriaResp resp = service.editRegistration(id);
			SptRegistration registration = resp.getRegistration();
			SptMemberType memberType = memberTypeRepository.findOne(registration.getMemberTypeId());
			Long renewlNum = sptMemberRenewalRepository.countRenewal(registration);
			
			byte[] data = new RegistrationForm(registration, memberType, renewlNum).createPdf();
			
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
