package com.may.ple.backend.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.catalina.util.URLEncoder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.ActionCodeFindCriteriaReq;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.NoticeDownloadCriteriaResp;
import com.may.ple.backend.criteria.ResultCodeFindCriteriaReq;
import com.may.ple.backend.criteria.ResultCodeGroupFindCriteriaReq;
import com.may.ple.backend.criteria.TraceCommentCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaResp;
import com.may.ple.backend.criteria.TraceResultCriteriaReq;
import com.may.ple.backend.criteria.TraceResultCriteriaResp;
import com.may.ple.backend.criteria.TraceSaveCriteriaReq;
import com.may.ple.backend.criteria.TraceSaveCriteriaResp;
import com.may.ple.backend.entity.ActionCode;
import com.may.ple.backend.entity.ResultCode;
import com.may.ple.backend.entity.ResultCodeGroup;
import com.may.ple.backend.service.CodeService;
import com.may.ple.backend.service.JasperService;
import com.may.ple.backend.service.ResultCodeGrouService;
import com.may.ple.backend.service.TraceWorkService;
import com.may.ple.backend.utils.TaskDetailStatusUtil;
import com.mongodb.BasicDBObject;

@Component
@Path("traceWork")
public class TraceWorkAction {
	private static final Logger LOG = Logger.getLogger(TraceWorkAction.class.getName());
	private TraceWorkService service;
	private CodeService codeService;
	private ResultCodeGrouService resultGroupService;
	private JasperService jasperService;
	
	@Autowired
	public TraceWorkAction(TraceWorkService service, CodeService codeService, ResultCodeGrouService resultGroupService, JasperService jasperService) {
		this.service = service;
		this.codeService = codeService;
		this.resultGroupService = resultGroupService;
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
			
			List<Integer> statuses = new ArrayList<>();
			statuses.add(1);
			
			LOG.debug("get ActionCode");
			ActionCodeFindCriteriaReq actionCodeReq = new ActionCodeFindCriteriaReq();
			actionCodeReq.setProductId(req.getProductId());
			actionCodeReq.setStatuses(statuses);			
			List<ActionCode> actionCodes = codeService.findActionCode(actionCodeReq);
			
			LOG.debug("get ResultCode");
			ResultCodeFindCriteriaReq resultCodeReq = new ResultCodeFindCriteriaReq();
			resultCodeReq.setProductId(req.getProductId());
			resultCodeReq.setStatuses(statuses);			
			List<ResultCode> resultCodes = codeService.findResultCode(resultCodeReq);
			
			LOG.debug("get ResultCodeGroup");
			ResultCodeGroupFindCriteriaReq resultCodeGroupFindCriteriaReq = new ResultCodeGroupFindCriteriaReq();
			resultCodeGroupFindCriteriaReq.setProductId(req.getProductId());
			List<ResultCodeGroup> resultCodeGroups = resultGroupService.find(resultCodeGroupFindCriteriaReq);
			
			resp.setActionCodes(actionCodes);
			resp.setResultCodes(resultCodes);
			resp.setResultCodeGroups(resultCodeGroups);
			
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
	
}