package com.may.ple.backend.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SptRegistrationEditCriteriaResp;
import com.may.ple.backend.pdf.RegistrationForm;
import com.may.ple.backend.repository.SptRegistrationRepository;

@Service
public class RegistrationFormReportService {
	private static final Logger LOG = Logger.getLogger(RegistrationFormReportService.class.getName());
	private SptRegistrationRepository sptRegistrationRepository;
	private SptRegistrationService service;
	
	@Autowired
	public RegistrationFormReportService(SptRegistrationRepository sptRegistrationRepository, SptRegistrationService service) {
		this.sptRegistrationRepository = sptRegistrationRepository;
		this.service = service;
	}
	
	public byte[] proceed(Long id) throws Exception {
		try {
			
			SptRegistrationEditCriteriaResp resp = service.editRegistration(id);
			byte[] data = new RegistrationForm(resp).createPdf();
			
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
