package com.may.ple.backend.service;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.ServiceDataFindCriteriaReq;
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
	
	public Page<ServiceData> findServiceData(ServiceDataFindCriteriaReq req) {
		PageRequest page = new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage(), Sort.Direction.DESC, "createdDateTime");
		return serviceDataRepository.findByserviceTypeId(page, req.getServiceTypeId());
	}
	
}
