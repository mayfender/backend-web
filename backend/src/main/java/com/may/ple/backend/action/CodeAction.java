package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CodeSaveCriteriaReq;
import com.may.ple.backend.criteria.CodeSaveCriteriaResp;
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
	
	/*@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public ProductSearchCriteriaResp findProduct(ProductSearchCriteriaReq req) {
		LOG.debug("Start");
		ProductSearchCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			resp = service.findProduct(req);
			
		} catch (Exception e) {
			resp = new ProductSearchCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}*/
	
	
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
	
	/*@POST
	@Path("/updateProduct")
	public CommonCriteriaResp updateProduct(PersistProductCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateProduct(req);
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
	
	@POST
	@Path("/deleteProduct")
	public ProductSearchCriteriaResp deleteProduct(ProductSearchCriteriaReq req) {
		LOG.debug("Start");
		ProductSearchCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteProduct(req.getProdId());
			
			resp = findProduct(req);
		} catch (Exception e) {
			resp = new ProductSearchCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}*/
	
}
