package com.may.ple.backend.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SptRegisteredFindCriteriaReq;
import com.may.ple.backend.entity.SptRegistration;
import com.may.ple.backend.excel.RegisterReport;

@Service
public class RegistrationExportReportService {
	private static final Logger LOG = Logger.getLogger(RegistrationExportReportService.class.getName());
	private SptRegistrationService service;
	@Value("${ext.template.register.excel}")
	private String templatePath;
	
	@Autowired
	public RegistrationExportReportService(SptRegistrationService service) {
		this.service = service;
	}
	
	public byte[] proceed(SptRegisteredFindCriteriaReq req) throws Exception {
		try {
			
			List<SptRegistration> regs = service.findRegisteredForExport(req);
			byte[] data = new RegisterReport(regs, templatePath).proceed();
			
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
