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
import com.may.ple.backend.criteria.PluginFindCriteriaReq;
import com.may.ple.backend.criteria.PluginFindCriteriaResp;
import com.may.ple.backend.criteria.ProgramFileFindCriteriaReq;
import com.may.ple.backend.criteria.ProgramFileFindCriteriaResp;
import com.may.ple.backend.service.PluginService;

@Component
@Path("plugin")
public class PluginAction {
	private static final Logger LOG = Logger.getLogger(PluginAction.class.getName());
	private PluginService service;
	
	@Autowired
	public PluginAction(PluginService service) {
		this.service = service;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(
			@FormDataParam("file") InputStream uploadedInputStream, 
			@FormDataParam("file") FormDataContentDisposition fileDetail, 
			@FormDataParam("module") String module) {
		
		LOG.debug("Start");
		PluginFindCriteriaResp resp = null;
		int status = 200;
		
		try {
			
			//--: Save to database
			LOG.debug("call save");
			service.save(uploadedInputStream, fileDetail, module);
			
			LOG.debug("Find task to show");
			PluginFindCriteriaReq req = new PluginFindCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			resp = service.find(req);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new PluginFindCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public PluginFindCriteriaResp find(PluginFindCriteriaReq req) {
		LOG.debug("Start");
		PluginFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.find(req);
		} catch (Exception e) {
			resp = new PluginFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
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
	
	@POST
	@Path("/delete")
	public PluginFindCriteriaResp delete(PluginFindCriteriaReq req) {
		LOG.debug("Start");
		PluginFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.delete(req.getId());
			
			resp = find(req);
		} catch (Exception e) {
			resp = new PluginFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/deploy")
	public CommonCriteriaResp deploy(@QueryParam("id")String id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			service.deploy(id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/start")
	public CommonCriteriaResp start(@QueryParam("id")String id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			service.start(id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/stop")
	public CommonCriteriaResp stop(@QueryParam("id")String id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			service.stop(id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateCommand")
	public CommonCriteriaResp updateCommand(PluginFindCriteriaReq req) {
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
	@Path("/updateEnabled")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateEnabled(PluginFindCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateEnabled(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}