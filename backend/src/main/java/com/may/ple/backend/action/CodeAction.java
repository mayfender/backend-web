package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.ActionCodeFindCriteriaReq;
import com.may.ple.backend.criteria.ActionCodeFindCriteriaResp;
import com.may.ple.backend.criteria.CodeSaveCriteriaReq;
import com.may.ple.backend.criteria.CodeSaveCriteriaResp;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.entity.ActionCode;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.CodeService;

@Component
@Path("code")
public class CodeAction {
	private static final Logger LOG = Logger.getLogger(CodeAction.class.getName());
	private CodeService service;
	
	@Autowired
	public CodeAction(CodeService service) {
		this.service = service;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public ActionCodeFindCriteriaResp find(ActionCodeFindCriteriaReq req) {
		LOG.debug("Start");
		ActionCodeFindCriteriaResp resp = new ActionCodeFindCriteriaResp();
		
		try {
			
			LOG.debug(req);
			List<ActionCode> actionCodes = service.find(req);
			resp.setActionCodes(actionCodes);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	
	@POST
	@Path("/saveCode")
	public CodeSaveCriteriaResp saveCode(CodeSaveCriteriaReq req) {
		LOG.debug("Start");
		CodeSaveCriteriaResp resp = new CodeSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.saveCode(req);
			
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
	
}