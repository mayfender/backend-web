package com.may.ple.backend.action;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
import com.may.ple.backend.criteria.TraceResultReportCriteriaResp;
import com.may.ple.backend.criteria.TraceResultReportFindCriteriaReq;
import com.may.ple.backend.criteria.TraceResultReportFindCriteriaResp;
import com.may.ple.backend.service.TraceResultReportService;

@Component
@Path("traceResultReport")
public class TraceResultReportAction {
	private static final Logger LOG = Logger.getLogger(TraceResultReportAction.class.getName());
	private TraceResultReportService service;
	
	@Autowired
	public TraceResultReportAction(TraceResultReportService service) {
		this.service = service;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail, 
			@FormDataParam("currentProduct") String currentProduct) {
		LOG.debug("Start");
		TraceResultReportFindCriteriaResp resp = null;
		int status = 200;
		
		try {
			LOG.debug(currentProduct);
			
			//--: Save to database
			LOG.debug("call save");
			service.save(uploadedInputStream, fileDetail, currentProduct);
			
			LOG.debug("Find task to show");
			TraceResultReportFindCriteriaReq req = new TraceResultReportFindCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			req.setProductId(currentProduct);
			resp = service.find(req);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new TraceResultReportFindCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/download")
	public Response download(TraceResultReportFindCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);
			
			boolean isFillTemplate = req.getIsFillTemplate() == null ? false : req.getIsFillTemplate();
			
			LOG.debug("Get file");
			Map<String, String> map = service.getFile(req);
			String fileName = map.get("fileName");
			String filePath = map.get("filePath");
			
//			Map<String, Object> taskDetail = null;
			/*if(isFillTemplate) {
				LOG.debug("Get taskDetail");
				TaskDetailViewCriteriaReq taskReq = new TaskDetailViewCriteriaReq();
				taskReq.setId(req.getTaskDetailId());
				taskReq.setProductId(req.getProductId());
				TaskDetailViewCriteriaResp taskResp = taskDetailService.getTaskDetailToNotice(taskReq);
				taskDetail = taskResp.getTaskDetail();
			}*/
			
			LOG.debug("Gen file");
			TraceResultReportCriteriaResp resp = new TraceResultReportCriteriaResp();
			resp.setFillTemplate(isFillTemplate);
			resp.setFilePath(filePath);
//			resp.setAddress(req.getAddress());
//			resp.setTaskDetail(taskDetail);
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public TraceResultReportFindCriteriaResp find(TraceResultReportFindCriteriaReq req) {
		LOG.debug("Start");
		TraceResultReportFindCriteriaResp resp;
		
		try {
			LOG.debug(req);	
			resp = service.find(req);
		} catch (Exception e) {
			resp = new TraceResultReportFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	
	
	@POST
	@Path("/updateEnabled")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateEnabled(TraceResultReportFindCriteriaReq req) {
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
	
	
	@POST
	@Path("/deleteFile")
	public TraceResultReportFindCriteriaResp deleteNoticeFile(TraceResultReportFindCriteriaReq req) {
		LOG.debug("Start");
		TraceResultReportFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteFileTask(req.getProductId(), req.getId());
			
			resp = find(req);
		} catch (Exception e) {
			resp = new TraceResultReportFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}
