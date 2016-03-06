package com.may.ple.backend.service;

import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.ServiceData;
import com.may.ple.backend.repository.ServiceDataRepository;

@Service
public class ServiceDataService {
	private static final Logger LOG = Logger.getLogger(ServiceDataService.class.getName());
	private ServiceDataRepository serviceDataRepository;
	private DataSource dataSource;
	
	@Autowired	
	public ServiceDataService(ServiceDataRepository serviceDataRepository, DataSource dataSource) {
		this.serviceDataRepository = serviceDataRepository;
		this.dataSource = dataSource;
	}
	
	public List<ServiceData> findUserAll() {
		return serviceDataRepository.findAll();
	}
	
}
