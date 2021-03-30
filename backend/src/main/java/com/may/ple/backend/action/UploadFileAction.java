package com.may.ple.backend.action;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.UploadFileCriteriaReq;
import com.may.ple.backend.criteria.UploadFileCriteriaResp;
import com.may.ple.backend.service.UploadFileService;

@Component
@Path("uploadFile")
public class UploadFileAction {
	private static final Logger LOG = Logger.getLogger(UploadFileAction.class.getName());
	private UploadFileService service;

	@Autowired
	public UploadFileAction(UploadFileService service) {
		this.service = service;
	}

	@POST
	@Path("/getFiles")
	@Produces(MediaType.APPLICATION_JSON)
	public UploadFileCriteriaResp getFiles(UploadFileCriteriaReq req) {
		LOG.debug("Start");
		UploadFileCriteriaResp resp = new UploadFileCriteriaResp();

		try {
			//---
			Map<String, Object> lastPeriod = service.getLastPeriod();
			resp.setLastPeriod(lastPeriod);
			req.setPeriodId(lastPeriod.get("_id").toString());

			//---
			resp.setOrderFiles(service.getFiles(req));
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
	public Response upload(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("periodId") String periodId,
			@FormDataParam("dealerId") String dealerId,
			@FormDataParam("customerName") String customerName
			) {
		LOG.debug("Start");
		UploadFileCriteriaResp resp = new UploadFileCriteriaResp();

		try {
			System.out.println();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp.setStatusCode(1000);
		}

		LOG.debug("End");
		return Response.status(200).entity(resp).build();
	}

}
