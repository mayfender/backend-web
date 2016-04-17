package com.may.ple.backend.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.SptRegistration;
import com.may.ple.backend.pdf.ReceiptRegistration;
import com.may.ple.backend.repository.SptRegistrationRepository;

@Service
public class SptRegistrationReceiptService {
	private static final Logger LOG = Logger.getLogger(SptRegistrationReceiptService.class.getName());
	private SptRegistrationRepository sptRegistrationRepository;
	
	@Autowired
	public SptRegistrationReceiptService(SptRegistrationRepository sptRegistrationRepository) {
		this.sptRegistrationRepository = sptRegistrationRepository;
	}
	
	public byte[] proceed(Long id) throws Exception {
		try {
			SptRegistration registration = sptRegistrationRepository.findOne(id);
			
			LOG.debug("firstName: " + registration.getFirstname());
			
			byte[] data = new ReceiptRegistration().createPdf();			
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
