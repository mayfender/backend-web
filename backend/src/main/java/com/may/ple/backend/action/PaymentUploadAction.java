package com.may.ple.backend.action;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
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

import com.google.gson.Gson;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.NoticeDownloadCriteriaResp;
import com.may.ple.backend.criteria.PaymentFindCriteriaReq;
import com.may.ple.backend.criteria.PaymentFindCriteriaResp;
import com.may.ple.backend.criteria.PaymentUpdateCriteriaReq;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.model.YearType;
import com.may.ple.backend.service.PaymentUploadService;

@Component
@Path("payment")
public class PaymentUploadAction {
	private static final Logger LOG = Logger.getLogger(PaymentUploadAction.class.getName());
	private PaymentUploadService service;

	@Autowired
	public PaymentUploadAction(PaymentUploadService service) {
		this.service = service;
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream,
						   @FormDataParam("file") FormDataContentDisposition fileDetail,
						   @FormDataParam("productId") String productId,
						   @FormDataParam("isConfirmImport") Boolean isConfirmImport,
						   @FormDataParam("yearTypes") String yearTypes) {
		LOG.debug("Start");
		PaymentFindCriteriaResp resp = null;
		int status = 200;

		try {
			LOG.debug(productId);
			List<YearType> yearT = null;

			if(isConfirmImport != null && isConfirmImport && yearTypes != null) {
				LOG.info("Parse yearType");
				yearT = Arrays.asList(new Gson().fromJson(yearTypes, YearType[].class));
			}

			//--: Save to database
			LOG.debug("call save");
			Map<String, Object> colData = service.save(uploadedInputStream, fileDetail, productId, isConfirmImport, yearT);

			LOG.debug("Find task to show");
			PaymentFindCriteriaReq req = new PaymentFindCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			req.setProductId(productId);
			resp = service.find(req);

			if(colData != null) {
				resp.setColDateTypes((List<ColumnFormat>)colData.get("colDateTypes"));
				resp.setColNotFounds((List<String>)colData.get("colNotFounds"));
			}
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new PaymentFindCriteriaResp(1000);
			status = 1000;
		}

		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}

	@POST
	@Path("/download")
	public Response download(PaymentFindCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);

			boolean isFillTemplate = req.getIsFillTemplate() == null ? false : req.getIsFillTemplate();

			LOG.debug("Get file");
			Map<String, String> map = service.getFile(req);
			String fileName = map.get("fileName");
			String filePath = map.get("filePath");

			LOG.debug("Gen file");
			NoticeDownloadCriteriaResp resp = new NoticeDownloadCriteriaResp();
			resp.setFillTemplate(isFillTemplate);
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
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public PaymentFindCriteriaResp find(PaymentFindCriteriaReq req) {
		LOG.debug("Start");
		PaymentFindCriteriaResp resp;

		try {
			LOG.debug(req);
			resp = service.find(req);
		} catch (Exception e) {
			resp = new PaymentFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/updateEnabled")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateEnabled(PaymentUpdateCriteriaReq req) {
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
	public PaymentFindCriteriaResp deleteFile(PaymentFindCriteriaReq req) {
		LOG.debug("Start");
		PaymentFindCriteriaResp resp;

		try {
			LOG.debug(req);
			service.deleteFileTask(req.getProductId(), req.getId());

			resp = service.find(req);
		} catch (Exception e) {
			resp = new PaymentFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

}
