package com.may.ple.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.may.ple.backend.bussiness.jasper.JasperReportEngine;
import com.may.ple.backend.model.DbFactory;

@Service
public class JasperService {
	private static final Logger LOG = Logger.getLogger(JasperService.class.getName());
	private MongoTemplate template;
	private DbFactory dbFactory;
	
	@Autowired	
	public JasperService(MongoTemplate template, DbFactory dbFactory) {
		this.template = template;
		this.dbFactory = dbFactory;
	}
	
	public byte[] exportNotice() throws Exception {
		try {
			LOG.debug("Start");
			
			String jasperFile = "C:/Users/sarawuti/Desktop/test_jasper/mayfender.jasper";
			Map<String, Object> params = new HashMap<>();
			params.put("nickName", "You can call me May.");
			
			LOG.debug("End");
			return new JasperReportEngine().toPdf(jasperFile, params);			
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
