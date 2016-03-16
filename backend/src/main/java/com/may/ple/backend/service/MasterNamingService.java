package com.may.ple.backend.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.MasterNaming;
import com.may.ple.backend.repository.MasterNamingRepository;

@Service
public class MasterNamingService {
	private static final Logger LOG = Logger.getLogger(MasterNamingService.class.getName());
	private MasterNamingRepository masterNamingRepository;
	
	@Autowired
	public MasterNamingService(MasterNamingRepository masterNamingRepository) {
		this.masterNamingRepository = masterNamingRepository;
	}
	
	public List<MasterNaming> findNamingActive() {
		return masterNamingRepository.findByStatus(0);
	}

}
