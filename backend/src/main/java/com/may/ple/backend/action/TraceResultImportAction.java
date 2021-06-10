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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.TraceResultImportFindCriteriaReq;
import com.may.ple.backend.criteria.TraceResultImportFindCriteriaResp;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.service.TraceResultImportService;

@Component
@Path("traceResultImport")
public class TraceResultImportAction {
	private static final Logger LOG = Logger.getLogger(TraceResultImportAction.class.getName());
	private TraceResultImportService service;
	private MongoTemplate templateCore;

	@Autowired
	public TraceResultImportAction(TraceResultImportService service, MongoTemplate templateCore) {
		this.service = service;
		this.templateCore = templateCore;
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream,
							@FormDataParam("file") FormDataContentDisposition fileDetail,
							@FormDataParam("productId") String productId,
							@FormDataParam("isAPIUpload") Boolean isAPIUpload
							) {
		LOG.debug("Start");
		TraceResultImportFindCriteriaResp resp = null;
		int status = 200;

		try {
			LOG.debug(productId);

			//--: Save to database
			LOG.debug("call save");
			service.save(uploadedInputStream, fileDetail, productId, isAPIUpload);

			LOG.debug("Find task to show");
			TraceResultImportFindCriteriaReq req = new TraceResultImportFindCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			req.setProductId(productId);
			resp = service.find(req);

		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new TraceResultImportFindCriteriaResp(1000);
			status = 1000;
		}

		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}

	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public TraceResultImportFindCriteriaResp find(TraceResultImportFindCriteriaReq req) {
		LOG.debug("Start");
		TraceResultImportFindCriteriaResp resp;

		try {
			LOG.debug(req);
			resp = service.find(req);

			//---: API Flag
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			Map krungSriAPISetting = product.getProductSetting().getKrungSriAPI();
			resp.setOnApi(krungSriAPISetting == null ? null : (int)krungSriAPISetting.get("enable"));
		} catch (Exception e) {
			resp = new TraceResultImportFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}


	@POST
	@Path("/deleteFile")
	public TraceResultImportFindCriteriaResp deleteFile(TraceResultImportFindCriteriaReq req) {
		LOG.debug("Start");
		TraceResultImportFindCriteriaResp resp;

		try {
			LOG.debug(req);
			service.deleteFileTask(req.getProductId(), req.getId());

			resp = service.find(req);
		} catch (Exception e) {
			resp = new TraceResultImportFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

}
