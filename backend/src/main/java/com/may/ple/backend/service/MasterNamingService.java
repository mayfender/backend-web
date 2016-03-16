package com.may.ple.backend.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.SptMasterNaming;
import com.may.ple.backend.repository.SptMasterNamingRepository;

@Service
public class MasterNamingService {
	private static final Logger LOG = Logger.getLogger(MasterNamingService.class.getName());
	private SptMasterNamingRepository masterNamingRepository;
	
	@Autowired
	public MasterNamingService(SptMasterNamingRepository masterNamingRepository) {
		this.masterNamingRepository = masterNamingRepository;
	}
	
	public List<SptMasterNaming> findNamingActive() {
		return masterNamingRepository.findByIsActive(1);
	}

}
