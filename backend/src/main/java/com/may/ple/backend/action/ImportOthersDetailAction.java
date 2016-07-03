package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.ImportOthersFindDetailCriteriaReq;
import com.may.ple.backend.criteria.ImportOthersFindDetailCriteriaResp;
import com.may.ple.backend.service.ImportOthersDetailService;

@Component
@Path("importOthersDetail")
public class ImportOthersDetailAction {
	private static final Logger LOG = Logger.getLogger(ImportOthersDetailAction.class.getName());
	private ImportOthersDetailService service;
	
	@Autowired
	public ImportOthersDetailAction(ImportOthersDetailService service) {
		this.service = service;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public ImportOthersFindDetailCriteriaResp find(ImportOthersFindDetailCriteriaReq req) {
		LOG.debug("Start");
		ImportOthersFindDetailCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.find(req);
		} catch (Exception e) {
			resp = new ImportOthersFindDetailCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	/*@POST
	@Path("/delete")
	public ImportOthersFindCriteriaResp delete(ImportOthersFindCriteriaReq req) {
		LOG.debug("Start");
		ImportOthersFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.delete(req.getProductId(), req.getId(), req.getMenuId());
			
			resp = find(req);
		} catch (Exception e) {
			resp = new ImportOthersFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}*/
		
}
