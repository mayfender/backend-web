package com.may.ple.backend.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.Zipcodes;
import com.may.ple.backend.repository.ZipCodesRepository;

@Service
public class ZipcodesService {
	private static final Logger LOG = Logger.getLogger(ZipcodesService.class.getName());
	private ZipCodesRepository zipCodesRepository;
	
	@Autowired
	public ZipcodesService(ZipCodesRepository zipCodesRepository) {
		this.zipCodesRepository = zipCodesRepository;
	}
	
	public List<Zipcodes> findByZipcode(String zipcode) {
		return zipCodesRepository.findByZipcode(zipcode);
	}
	
}
