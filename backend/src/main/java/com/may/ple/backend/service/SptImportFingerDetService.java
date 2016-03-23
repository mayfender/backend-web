package com.may.ple.backend.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.repository.SptImportFingerDetRepository;

@Service
public class SptImportFingerDetService {
	private static final Logger LOG = Logger.getLogger(SptImportFingerDetService.class.getName());
	private SptImportFingerDetRepository sptImportFingerDetRepository;
	
	@Autowired	
	public SptImportFingerDetService(SptImportFingerDetRepository sptImportFingerDetRepository) {
		this.sptImportFingerDetRepository = sptImportFingerDetRepository;
	}
	
}
