package com.may.ple.backend.action;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.ActionCodeFindCriteriaReq;
import com.may.ple.backend.criteria.ResultCodeFindCriteriaReq;
import com.may.ple.backend.criteria.ResultCodeGroupFindCriteriaReq;
import com.may.ple.backend.criteria.TraceWordPrepareDataCriteriaReq;
import com.may.ple.backend.criteria.TraceWorkPrepareDataCriteriaResp;
import com.may.ple.backend.entity.ActionCode;
import com.may.ple.backend.entity.ResultCode;
import com.may.ple.backend.entity.ResultCodeGroup;
import com.may.ple.backend.service.CodeService;
import com.may.ple.backend.service.ResultCodeGrouService;
import com.may.ple.backend.service.TraceWorkService;

@Component
@Path("traceWork")
public class TraceWorkAction {
	private static final Logger LOG = Logger.getLogger(TraceWorkAction.class.getName());
	private TraceWorkService service;
	private ResultCodeGrouService resultGroupService;
	private CodeService codeService;
	
	@Autowired
	public TraceWorkAction(TraceWorkService service, ResultCodeGrouService resultGroupService, CodeService codeService) {
		this.service = service;
		this.resultGroupService = resultGroupService;
		this.codeService = codeService;
	}
	
	@POST
	@Path("/prepareData")
	@Produces(MediaType.APPLICATION_JSON)
	public TraceWorkPrepareDataCriteriaResp prepareData(TraceWordPrepareDataCriteriaReq req) {
		LOG.debug("Start");
		TraceWorkPrepareDataCriteriaResp resp = new TraceWorkPrepareDataCriteriaResp();
		
		try {
			
			LOG.debug(req);
			
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
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	
	
	/*@POST
	@Path("/saveActionCode")
	public CodeSaveCriteriaResp saveActionCode(CodeSaveCriteriaReq req) {
		LOG.debug("Start");
		CodeSaveCriteriaResp resp = new CodeSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.saveActionCode(req);
			
			resp.setId(id);
		} catch (CustomerException cx) {
			resp.setStatusCode(cx.errCode);
			LOG.error(cx.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}*/
	
	/*@POST
	@Path("/saveResultCode")
	public CodeSaveCriteriaResp saveResultCode(CodeSaveCriteriaReq req) {
		LOG.debug("Start");
		CodeSaveCriteriaResp resp = new CodeSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.saveResultCode(req);
			
			resp.setId(id);
		} catch (CustomerException cx) {
			resp.setStatusCode(cx.errCode);
			LOG.error(cx.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/deleteActionCode")
	public CommonCriteriaResp deleteActionCode(@QueryParam("id")String id, @QueryParam("productId")String productId) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			service.deleteActionCode(id, productId);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/deleteResultCode")
	public CommonCriteriaResp deleteResultCode(@QueryParam("id")String id, @QueryParam("productId")String productId) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			service.deleteResultCode(id, productId);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}*/
	
}