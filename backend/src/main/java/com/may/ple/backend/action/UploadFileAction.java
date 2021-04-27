package com.may.ple.backend.action;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.OrderCriteriaReq;
import com.may.ple.backend.criteria.UploadFileCriteriaReq;
import com.may.ple.backend.criteria.UploadFileCriteriaResp;
import com.may.ple.backend.service.OrderService;
import com.may.ple.backend.service.UploadFileService;

@Component
@Path("uploadFile")
public class UploadFileAction {
	private static final Logger LOG = Logger.getLogger(UploadFileAction.class.getName());
	private UploadFileService service;
	private OrderService ordService;

	@Autowired
	public UploadFileAction(UploadFileService service, OrderService ordService) {
		this.service = service;
		this.ordService = ordService;
	}

	@POST
	@Path("/getFiles")
	@Produces(MediaType.APPLICATION_JSON)
	public UploadFileCriteriaResp getFiles(UploadFileCriteriaReq req) {
		LOG.debug("Start");
		UploadFileCriteriaResp resp = new UploadFileCriteriaResp();

		try {
			//---
			if(StringUtils.isBlank(req.getPeriodId())) {
				LOG.debug("Get Last Period");
				Map<String, Object> lastPeriod = service.getLastPeriod();
				resp.setLastPeriod(lastPeriod);
				req.setPeriodId(lastPeriod.get("_id").toString());
			}

			//---
			List<Integer> staus = new ArrayList<>();
			if(req.getStatus() != null) {
				staus.add(req.getStatus());
			} else {
				staus.add(0);
				staus.add(1);
				staus.add(2);
			}
			UploadFileCriteriaResp respFile = service.getFiles(req, staus);
			if(respFile != null) {
				resp.setOrderFiles(respFile.getOrderFiles());
				resp.setTotalItems(respFile.getTotalItems());

				resp.setCustomerNameLst(service.getCustomerNameByPeriod(req.getPeriodId(), req.getDealerId(), staus));
			}
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/removeFile")
	@Produces(MediaType.APPLICATION_JSON)
	public UploadFileCriteriaResp removeFile(UploadFileCriteriaReq req) {
		LOG.debug("Start");
		UploadFileCriteriaResp resp = new UploadFileCriteriaResp();

		try {
			int errorCode = service.removeFile(req);
			resp.setErrCode(errorCode);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/viewImage")
	@Produces(MediaType.APPLICATION_JSON)
	public UploadFileCriteriaResp viewImage(UploadFileCriteriaReq req) {
		LOG.debug("Start");
		UploadFileCriteriaResp resp = new UploadFileCriteriaResp();

		try {
			resp.setBase64Data(service.viewImage(req));
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
			service.saveFile(uploadedInputStream, fileDetail, periodId, dealerId, customerName);
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp.setStatusCode(1000);
		}

		LOG.debug("End");
		return Response.status(200).entity(resp).build();
	}

	@POST
	@Path("/getImageAndFlag")
	@Produces(MediaType.APPLICATION_JSON)
	public UploadFileCriteriaResp getImageAndFlag(UploadFileCriteriaReq req) {
		LOG.debug("Start");
		UploadFileCriteriaResp resp = new UploadFileCriteriaResp();

		try {
			resp.setOrderFile(service.getNextImage(req));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/getCurrentImage")
	@Produces(MediaType.APPLICATION_JSON)
	public UploadFileCriteriaResp getCurrentImage(UploadFileCriteriaReq req) {
		LOG.debug("Start");
		UploadFileCriteriaResp resp = new UploadFileCriteriaResp();

		try {
			LOG.debug("Get Last Period");
			Map<String, Object> lastPeriod = service.getLastPeriod();
			resp.setLastPeriod(lastPeriod);
			req.setPeriodId(lastPeriod.get("_id").toString());

			resp.setOrderFile(service.getCurrentImage(req));

			OrderCriteriaReq ordReq = new OrderCriteriaReq();
			ordReq.setPeriodId(req.getPeriodId());
			ordReq.setDealerId(req.getDealerId());

			Map<String, Integer> orderFileSum = ordService.orderFileSum(ordReq);
			resp.setOrderFileSum(orderFileSum);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

}
