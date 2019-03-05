package com.may.ple.backend.action;

import java.util.ArrayList;
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
import com.may.ple.backend.criteria.DymSearchCriteriaResp;
import com.may.ple.backend.criteria.DymSearchValueCriteriaResp;
import com.may.ple.backend.criteria.ListSaveCriteriaReq;
import com.may.ple.backend.criteria.ListSaveCriteriaResp;
import com.may.ple.backend.criteria.SearchValueSaveCriteriaReq;
import com.may.ple.backend.entity.DymSearch;
import com.may.ple.backend.entity.DymSearchValue;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.DymSearchService;

@Component
@Path("dymSearch")
public class DymSearchAction {
	private static final Logger LOG = Logger.getLogger(DymSearchAction.class.getName());
	private DymSearchService service;
	
	@Autowired
	public DymSearchAction(DymSearchService service) {
		this.service = service;
	}
	
	@GET
	@Path("/getFields")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp getFields(@QueryParam("productId")String productId) {
		LOG.debug("Start");
		DymSearchCriteriaResp resp = new DymSearchCriteriaResp();
		
		try {
			List<Integer> statuses = new ArrayList<>();
			statuses.add(0);
			statuses.add(1);
			
			List<DymSearch> dymList = service.findList(productId, statuses);
			resp.setDymSearch(dymList);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/saveField")
	public ListSaveCriteriaResp saveField(ListSaveCriteriaReq req) {
		LOG.debug("Start");
		ListSaveCriteriaResp resp = new ListSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.saveList(req);
			
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
	@Path("/deleteField")
	public CommonCriteriaResp deleteField(@QueryParam("id")String id, @QueryParam("productId")String productId) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			service.deleteField(id, productId);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getValues")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp getValues(@QueryParam("productId")String productId, @QueryParam("fieldId")String fieldId) {
		LOG.debug("Start");
		DymSearchValueCriteriaResp resp = new DymSearchValueCriteriaResp();
		
		try {
			List<DymSearchValue> dymSearchValue = service.getValues(productId, fieldId);			
			resp.setDymSearchValue(dymSearchValue);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/saveValue")
	public ListSaveCriteriaResp saveValue(SearchValueSaveCriteriaReq req) {
		LOG.debug("Start");
		ListSaveCriteriaResp resp = new ListSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.saveValue(req);
			
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
	@Path("/deleteValue")
	public CommonCriteriaResp deleteValue(@QueryParam("id")String id, @QueryParam("productId")String productId) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			service.deleteValue(id, productId);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}