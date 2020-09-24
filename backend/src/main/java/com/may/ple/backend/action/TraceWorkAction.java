package com.may.ple.backend.action;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
import com.may.ple.backend.criteria.TraceCommentCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaResp;
import com.may.ple.backend.criteria.TraceHisFindCriteriaResp;
import com.may.ple.backend.criteria.TraceResultCriteriaReq;
import com.may.ple.backend.criteria.TraceResultCriteriaResp;
import com.may.ple.backend.criteria.TraceSaveCriteriaReq;
import com.may.ple.backend.criteria.TraceSaveCriteriaResp;
import com.may.ple.backend.criteria.UpdateTraceResultCriteriaReq;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.JasperService;
import com.may.ple.backend.service.TraceWorkService;
import com.may.ple.backend.utils.TaskDetailStatusUtil;
import com.mongodb.BasicDBObject;

@Component
@Path("traceWork")
public class TraceWorkAction {
	private static final Logger LOG = Logger.getLogger(TraceWorkAction.class.getName());
	private TraceWorkService service;
	private JasperService jasperService;

	@Autowired
	public TraceWorkAction(TraceWorkService service, JasperService jasperService) {
		this.service = service;
		this.jasperService = jasperService;
	}

	@POST
	@Path("/find")
	public TraceFindCriteriaResp find(TraceFindCriteriaReq req) {
		LOG.debug("Start");
		TraceFindCriteriaResp resp = null;

		try {

			LOG.debug(req);
			resp = service.find(req);

		} catch (Exception e) {
			resp = new TraceFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@GET
	@Path("/getHis")
	public TraceHisFindCriteriaResp getHis(@QueryParam("productId") String productId, @QueryParam("id") String id) {
		LOG.debug("Start");
		TraceHisFindCriteriaResp resp = new TraceHisFindCriteriaResp();

		try {
			List<Map> his = service.getHis(productId, id);

			//--: Reverse order to make the front-end easy to use.
			List<Map> result = new ArrayList<>();
			for (int i = (his.size() - 1); i >= 0; i--) result.add(his.get(i));

			resp.setTraceWorkHises(result);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/save")
	public TraceSaveCriteriaResp save(TraceSaveCriteriaReq req) {
		LOG.debug("Start");
		TraceSaveCriteriaResp resp = new TraceSaveCriteriaResp();

		try {
			LOG.debug(req);
			service.save(req);

			int traceStatus = TaskDetailStatusUtil.getStatus(req.getAppointDate(), req.getNextTimeDate());
			resp.setTraceStatus(traceStatus);
			resp.setTraceDate(req.getTraceDate());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/delete")
	public TraceFindCriteriaResp delete(TraceFindCriteriaReq req) {
		LOG.debug("Start");
		TraceFindCriteriaResp resp = null;

		try {

			LOG.debug(req);
			service.delete(req.getId(), req.getProductId(), req.getContractNo(), req.getTaskDetailId());
			resp = service.find(req);

		} catch (Exception e) {
			resp = new TraceFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/traceResult")
	@Produces(MediaType.APPLICATION_JSON)
	public TraceResultCriteriaResp traceResult(TraceResultCriteriaReq req) {
		LOG.debug("Start");
		TraceResultCriteriaResp resp;

		try {
			LOG.debug(req);
			resp = service.traceResult(req, null, false);
		} catch (Exception e) {
			resp = new TraceResultCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/exportNotices")
	public Response exportNotices(TraceResultCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);

			BasicDBObject fields = new BasicDBObject();
			fields.append("resultText", 1);
			fields.append("appointDate", 1);
			fields.append("appointAmount", 1);
			fields.append("tel", 1);
			fields.append("nextTimeDate", 1);
			fields.append("createdDateTime", 1);
			fields.append("link_actionCode.actCode", 1);
			fields.append("link_resultCode.rstCode", 1);
			fields.append("templateId", 1);
			fields.append("addressNoticeStr", 1);

			LOG.debug("Call traceResult");
			req.setCurrentPage(null);
			TraceResultCriteriaResp traceResp = service.traceResult(req, fields, true);
			List<Map> traceDatas = traceResp.getTraceDatas();

			if(traceDatas == null) return Response.status(404).build();

			LOG.debug("Call exportNotices");
			NoticeDownloadCriteriaResp resp = service.exportNotices(jasperService, req.getProductId(), traceDatas);

			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode("template"));

			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}

	@POST
	@Path("/updateComment")
	public CommonCriteriaResp updateComment(TraceCommentCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};

		try {

			LOG.debug(req);
			service.updateComment(req);

		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/updateHold")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateHold(UpdateTraceResultCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};

		try {
			LOG.debug(req);
			service.updateHold(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}


	@POST
	@Path("/updateTaskDetail")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateTaskDetail(TraceResultCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};

		try {
			LOG.debug(req);

			LOG.debug("Call traceResult");
			req.setCurrentPage(null);
			TraceResultCriteriaResp traceResult = service.traceResult(req, null, true);
			List<Map> traceDatas = traceResult.getTraceDatas();

			if(traceDatas != null) {
				LOG.debug("Call updateTaskDetail");
				service.updateTaskDetail(req.getProductId(), traceDatas);
			}
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/traceUpload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response traceUpload(
								@FormDataParam("file") InputStream uploadedInputStream,
								@FormDataParam("file") FormDataContentDisposition fileDetail,
								@FormDataParam("productId") String productId
							) {
		LOG.debug("Start");
		TraceResultCriteriaResp resp = new TraceResultCriteriaResp();

		try {
			LOG.debug("Call traceUpload");
			service.traceUpload(uploadedInputStream, fileDetail, productId);
		} catch (CustomerException e) {
			if(e.errCode == 3000) {
//				resp.setCommonMsg(e.getMessage());
			}
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp.setStatusCode(1000);
		}

		LOG.debug("End");
		return Response.status(200).entity(resp).build();
	}

}