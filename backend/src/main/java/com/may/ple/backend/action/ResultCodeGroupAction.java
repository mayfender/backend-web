package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.ResultCodeGroupFindCriteriaReq;
import com.may.ple.backend.criteria.ResultCodeGroupFindCriteriaResp;
import com.may.ple.backend.criteria.ResultCodeGroupSaveCriteriaReq;
import com.may.ple.backend.criteria.ResultCodeGroupSaveCriteriaResp;
import com.may.ple.backend.entity.ResultCodeGroup;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.ResultCodeGrouService;

@Component
@Path("resultCodeGroup")
public class ResultCodeGroupAction {
	private static final Logger LOG = Logger.getLogger(ResultCodeGroupAction.class.getName());
	private ResultCodeGrouService service;
	
	@Autowired
	public ResultCodeGroupAction(ResultCodeGrouService service) {
		this.service = service;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public ResultCodeGroupFindCriteriaResp find(ResultCodeGroupFindCriteriaReq req) {
		LOG.debug("Start");
		ResultCodeGroupFindCriteriaResp resp = new ResultCodeGroupFindCriteriaResp();
		
		try {
			
			LOG.debug(req);
			List<ResultCodeGroup> resultCodeGroups = service.find(req);
			resp.setResultCodeGroups(resultCodeGroups);
			
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
	public ResultCodeGroupSaveCriteriaResp save(ResultCodeGroupSaveCriteriaReq req) {
		LOG.debug("Start");
		ResultCodeGroupSaveCriteriaResp resp = new ResultCodeGroupSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.save(req);
			
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
	@Path("/delete")
	public CommonCriteriaResp delete(@QueryParam("id")String id, @QueryParam("productId")String productId) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			service.delete(id, productId);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}