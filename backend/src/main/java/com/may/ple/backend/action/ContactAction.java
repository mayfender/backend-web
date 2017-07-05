package com.may.ple.backend.action;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.BankAccCriteriaResp;
import com.may.ple.backend.criteria.BankAccSaveCriteriaReq;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.CustomerComSaveCriteriaReq;
import com.may.ple.backend.criteria.ListSaveCriteriaResp;
import com.may.ple.backend.criteria.SentMailCriteriaReq;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.service.ContactService;
import com.may.ple.backend.utils.FileUtil;

@Component
@Path("contact")
public class ContactAction {
	private static final Logger LOG = Logger.getLogger(ContactAction.class.getName());
	private ContactService service;
	
	@Autowired
	public ContactAction(ContactService service) {
		this.service = service;
	}
	
	@POST
	@Path("/sentMail")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp sentMail(SentMailCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug(req);
			service.sentMail(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/sentMailAttach")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp sentMailAttach(@FormDataParam("file") InputStream in,
											 @FormDataParam("file") FormDataContentDisposition fileDetail,
											 @FormDataParam("name") String name,
											 @FormDataParam("mobile") String mobile,
											 @FormDataParam("line") String line,
											 @FormDataParam("email") String email,
											 @FormDataParam("detail") String detail) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			SentMailCriteriaReq req = new SentMailCriteriaReq();
			req.setName(name);
			req.setMobile(mobile);
			req.setLine(line);
			req.setEmail(email);
			req.setDetail(detail);
			
			FileDetail fd = FileUtil.getFileName(fileDetail, null);
			
			LOG.debug(req);
			service.sentMailAndAttach(req, in, fd);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/findAccNo")
	@Produces(MediaType.APPLICATION_JSON)
	public BankAccCriteriaResp findAccNo() {
		LOG.debug("Start");
		BankAccCriteriaResp resp = null;
		
		try {			
			resp = service.findAccNo();
		} catch (Exception e) {
			resp = new BankAccCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/saveAccNo")
	@Produces(MediaType.APPLICATION_JSON)
	public ListSaveCriteriaResp saveAccNo(BankAccSaveCriteriaReq req) {
		LOG.debug("Start");
		ListSaveCriteriaResp resp = new ListSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.saveAccNo(req);
			
			resp.setId(id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateCusCompany")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateCusCompany(CustomerComSaveCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug(req);
			service.updateCusCompany(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateCusCompanyEmail")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateCusCompanyEmail(CustomerComSaveCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug(req);
			service.updateCusCompanyEmail(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/deleteAccNo")
	public CommonCriteriaResp deleteAccNo(@QueryParam("id")String id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			service.deleteAccNo(id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}