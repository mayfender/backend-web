package com.may.ple.backend.action;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.catalina.util.URLEncoder;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.NoticeDownloadCriteriaResp;
import com.may.ple.backend.criteria.ProgramFileFindCriteriaReq;
import com.may.ple.backend.criteria.ProgramFileFindCriteriaResp;
import com.may.ple.backend.service.ProgramService;

@Component
@Path("program")
public class ProgramAction {
	private static final Logger LOG = Logger.getLogger(ProgramAction.class.getName());
	private ProgramService service;
	
	@Autowired
	public ProgramAction(ProgramService service) {
		this.service = service;
	}
	
	@GET
	@Path("/deploy")
	public CommonCriteriaResp deploy(@QueryParam("id")String id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			
			LOG.info("Call deploy");
			service.deploy(id);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/deployDeploy")
	public CommonCriteriaResp deployDeploy(@QueryParam("id")String id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			
			LOG.info("Call deploy");
			service.deployDeploy(id);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/deployTunnel")
	public CommonCriteriaResp deployTunnel(@QueryParam("id")String id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			
			LOG.info("Call deploy");
			service.deployTunnel(id);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		LOG.debug("Start");
		ProgramFileFindCriteriaResp resp = null;
		int status = 200;
		
		try {
			
			//--: Save to database
			LOG.debug("call save");
			service.save(uploadedInputStream, fileDetail);
			
			LOG.debug("Find task to show");
			ProgramFileFindCriteriaReq req = new ProgramFileFindCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			resp = service.findAll(req);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new ProgramFileFindCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/uploadDeployer")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadDeployer(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		LOG.debug("Start");
		ProgramFileFindCriteriaResp resp = null;
		int status = 200;
		
		try {
			
			//--: Save to database
			LOG.debug("call save");
			service.saveDeployer(uploadedInputStream, fileDetail);
			
			LOG.debug("Find task to show");
			ProgramFileFindCriteriaReq req = new ProgramFileFindCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			resp = service.findAllDeployer(req);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new ProgramFileFindCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/uploadTunnel")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadTunnel(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		LOG.debug("Start");
		ProgramFileFindCriteriaResp resp = null;
		int status = 200;
		
		try {
			
			//--: Save to database
			LOG.debug("call save");
			service.saveTunnel(uploadedInputStream, fileDetail);
			
			LOG.debug("Find task to show");
			ProgramFileFindCriteriaReq req = new ProgramFileFindCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			resp = service.findAllTunnel(req);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new ProgramFileFindCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/findAll")
	@Produces(MediaType.APPLICATION_JSON)
	public ProgramFileFindCriteriaResp findAll(ProgramFileFindCriteriaReq req) {
		LOG.debug("Start");
		ProgramFileFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.findAll(req);
		} catch (Exception e) {
			resp = new ProgramFileFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/findAllDeployer")
	@Produces(MediaType.APPLICATION_JSON)
	public ProgramFileFindCriteriaResp findAllDeployer(ProgramFileFindCriteriaReq req) {
		LOG.debug("Start");
		ProgramFileFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.findAllDeployer(req);
		} catch (Exception e) {
			resp = new ProgramFileFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/findAllTunnel")
	@Produces(MediaType.APPLICATION_JSON)
	public ProgramFileFindCriteriaResp findAllTunnel(ProgramFileFindCriteriaReq req) {
		LOG.debug("Start");
		ProgramFileFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.findAllTunnel(req);
		} catch (Exception e) {
			resp = new ProgramFileFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/delete")
	public ProgramFileFindCriteriaResp delete(ProgramFileFindCriteriaReq req) {
		LOG.debug("Start");
		ProgramFileFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.delete(req.getId());
			
			if(req.getIsDeployer() != null && req.getIsDeployer()) {
				resp = findAllDeployer(req);
			} else if(req.getIsTunnel() != null && req.getIsTunnel()) {
				resp = findAllTunnel(req);
			} else {
				resp = findAll(req);
			}
		} catch (Exception e) {
			resp = new ProgramFileFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateCommand")
	public CommonCriteriaResp updateCommand(ProgramFileFindCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateCommand(req);
		} catch (Exception e) {
			resp = new ProgramFileFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/download")
	public Response download(ProgramFileFindCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);
			
			LOG.debug("Get file");
			Map<String, String> map = service.getFile(req);
			String fileName = map.get("fileName");
			String filePath = map.get("filePath");
			
			LOG.debug("Gen file");
			NoticeDownloadCriteriaResp resp = new NoticeDownloadCriteriaResp();
			resp.setFilePath(filePath);
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
}