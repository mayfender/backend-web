package com.may.ple.backend.action;

import javax.mail.internet.MimeMessage;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.SptRegisteredFindCriteriaReq;
import com.may.ple.backend.criteria.SptRegisteredFindCriteriaResp;
import com.may.ple.backend.criteria.SptRegistrationEditCriteriaResp;
import com.may.ple.backend.criteria.SptRegistrationSaveCriteriaReq;
import com.may.ple.backend.criteria.SptRegistrationSaveCriteriaResp;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.SptRegistrationService;

@Component
@Path("registration")
public class SptRegistrationAction {
	private static final Logger LOG = Logger.getLogger(SptRegistrationAction.class.getName());
	private SptRegistrationService service;
	private JavaMailSender mailSender;
	
	@Autowired
	public SptRegistrationAction(SptRegistrationService service, JavaMailSender mailSender) {
		this.service = service;
		this.mailSender = mailSender;
	}
	
	@POST
	@Path("/findRegistered")
	@Produces(MediaType.APPLICATION_JSON)
	public SptRegisteredFindCriteriaResp findRegistered(SptRegisteredFindCriteriaReq req) {
		LOG.debug("Start");
		SptRegisteredFindCriteriaResp resp = new SptRegisteredFindCriteriaResp();
		
		try {
			
			LOG.debug(req);
			resp = service.findRegistered(req);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/saveRegistration")
	@Produces(MediaType.APPLICATION_JSON)
	public SptRegistrationSaveCriteriaResp saveRegistration(SptRegistrationSaveCriteriaReq req) {
		LOG.debug("Start");
		SptRegistrationSaveCriteriaResp resp = new SptRegistrationSaveCriteriaResp(){};
		
		try {
			
			LOG.debug(req);
			Long regId = service.saveRegistration(req);
			resp.setRegId(regId);
			
		} catch (CustomerException e) {			
			resp.setStatusCode(e.errCode);
			LOG.error(e.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/editRegistration")
	@Produces(MediaType.APPLICATION_JSON)
	public SptRegistrationEditCriteriaResp editRegistration(@QueryParam("id")Long id) {
		LOG.debug("Start");
		SptRegistrationEditCriteriaResp resp = null;
		
		try {
			
			LOG.debug("id: " + id);
			resp = service.editRegistration(id);
			
		} catch (Exception e) {
			resp = new SptRegistrationEditCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateRegistration")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateRegistration(SptRegistrationSaveCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			
			LOG.debug(req);
			service.updateRegistration(req);
			
			MimeMessage mail = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mail, true);
	        helper.setTo("mayfender@gmail.com");
	        //helper.setReplyTo("someone@localhost");
//	        helper.setFrom("mayfender.work@gmail.com");
	        helper.setSubject("Lorem ipsum");
	        helper.setText("Lorem ipsum dolor sit amet [...]");
	        
	        mailSender.send(mail);
			
		} catch (CustomerException e) {			
			resp.setStatusCode(e.errCode);
			LOG.error(e.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/deleteRegistration")
	@Produces(MediaType.APPLICATION_JSON)
	public SptRegisteredFindCriteriaResp deleteRegistration(SptRegisteredFindCriteriaReq req) {
		LOG.debug("Start");
		SptRegisteredFindCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			service.deleteRegistration(req.getRegId());
			
			resp = findRegistered(req);
		} catch (Exception e) {
			resp = new SptRegisteredFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/findRenewal")
	@Produces(MediaType.APPLICATION_JSON)
	public SptRegisteredFindCriteriaResp findRenewal(SptRegisteredFindCriteriaReq req) {
		LOG.debug("Start");
		SptRegisteredFindCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			resp = service.findRenewal(req);
			
			service.period(resp.getRegistereds());
			
		} catch (Exception e) {
			resp = new SptRegisteredFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/memberIdCheckExist")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp memberIdCheckExist(@QueryParam("memberId")String memberId, @QueryParam("id") Long id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug("memberId: " + memberId + ", ID: " +id);
			boolean isExist = service.memberIdCheckExist(memberId, id);
			
			if(isExist) {
				resp.setStatusCode(2000);
			}
		} catch (Exception e) {
			resp = new SptRegistrationEditCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}
