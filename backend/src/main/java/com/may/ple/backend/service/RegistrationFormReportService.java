package com.may.ple.backend.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.pdf.RegistrationForm;
import com.may.ple.backend.repository.SptRegistrationRepository;

@Service
public class RegistrationFormReportService {
	private static final Logger LOG = Logger.getLogger(RegistrationFormReportService.class.getName());
	private SptRegistrationRepository sptRegistrationRepository;
	
	@Autowired
	public RegistrationFormReportService(SptRegistrationRepository sptRegistrationRepository) {
		this.sptRegistrationRepository = sptRegistrationRepository;
	}
	
	public byte[] proceed(Long id) throws Exception {
		try {
			
			byte[] data = new RegistrationForm(null, null).createPdf();
			
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
