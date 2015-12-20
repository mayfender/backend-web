package com.may.ple.backend.action;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.LoadDataCriteriaResp;
import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.service.LoadDataService;

@Component
@Path("loadData")
public class LoadDataAction {
	private static final Logger LOG = Logger.getLogger(LoadDataAction.class.getName());
	private LoadDataService service;
	
	@Autowired
	public LoadDataAction(LoadDataService service) {
		this.service = service;
	}
	
	@GET
	@Path("/load")
	public LoadDataCriteriaResp load() {
		LOG.debug("Start");
		LoadDataCriteriaResp resp = new LoadDataCriteriaResp();
		
		try {
			
			Map<String, List<Menu>> menus = service.loadMenu();
			resp.setMenus(menus);			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}
