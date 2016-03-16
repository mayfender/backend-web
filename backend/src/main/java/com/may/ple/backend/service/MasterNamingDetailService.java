package com.may.ple.backend.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.MasterNamingDetail;
import com.may.ple.backend.repository.MasterNamingDetailRepository;

@Service
public class MasterNamingDetailService {
	private static final Logger LOG = Logger.getLogger(MasterNamingDetailService.class.getName());
	private MasterNamingDetailRepository masterNamingDetailRepository;
	
	@Autowired
	public MasterNamingDetailService(MasterNamingDetailRepository masterNamingDetailRepository) {
		this.masterNamingDetailRepository = masterNamingDetailRepository;
	}
	
	public List<MasterNamingDetail> findByMasterId(Long id) {
		return masterNamingDetailRepository.findByNamingId(id);
	}

}
