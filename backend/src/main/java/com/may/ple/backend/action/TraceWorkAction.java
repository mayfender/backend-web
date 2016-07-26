package com.may.ple.backend.action;

import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.service.TraceWorkService;

@Component
@Path("traceWork")
public class TraceWorkAction {
	private static final Logger LOG = Logger.getLogger(TraceWorkAction.class.getName());
	private TraceWorkService service;
	
	@Autowired
	public TraceWorkAction(TraceWorkService service) {
		this.service = service;
	}
	
	
	/*@POST
	@Path("/saveActionCode")
	public CodeSaveCriteriaResp saveActionCode(CodeSaveCriteriaReq req) {
		LOG.debug("Start");
		CodeSaveCriteriaResp resp = new CodeSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.saveActionCode(req);
			
			resp.setId(id);
		} catch (CustomerException cx) {
			resp.setStatusCode(cx.errCode);
			LOG.error(cx.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}*/
	
	/*@POST
	@Path("/saveResultCode")
	public CodeSaveCriteriaResp saveResultCode(CodeSaveCriteriaReq req) {
		LOG.debug("Start");
		CodeSaveCriteriaResp resp = new CodeSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.saveResultCode(req);
			
			resp.setId(id);
		} catch (CustomerException cx) {
			resp.setStatusCode(cx.errCode);
			LOG.error(cx.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/deleteActionCode")
	public CommonCriteriaResp deleteActionCode(@QueryParam("id")String id, @QueryParam("productId")String productId) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			service.deleteActionCode(id, productId);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/deleteResultCode")
	public CommonCriteriaResp deleteResultCode(@QueryParam("id")String id, @QueryParam("productId")String productId) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			service.deleteResultCode(id, productId);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}*/
	
}