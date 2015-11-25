package com.may.ple.backend.action;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.GetImageCriteriaResp;
import com.may.ple.backend.criteria.MenuCriteriaReq;
import com.may.ple.backend.criteria.MenuCriteriaResp;
import com.may.ple.backend.service.MenuService;

@Component
@Path("menu")
public class MenuAction {
	private static final Logger LOG = Logger.getLogger(MenuAction.class.getName());
	private MenuService menuService;
	
	@Autowired
	public MenuAction(MenuService menuService) {
		this.menuService = menuService;
	}
	
	@POST
	@Path("/searchMenu")
	public MenuCriteriaResp searchMenu(MenuCriteriaReq req) {
		LOG.debug("Start");
		MenuCriteriaResp resp;
		
		try {
			LOG.debug(req);
			
			resp = menuService.searchMenu(req);
			
			LOG.debug(resp);
		} catch (Exception e) {
			resp = new MenuCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getImage")
	public GetImageCriteriaResp getImage(@QueryParam("id") long id) {
		LOG.debug("Start");
		GetImageCriteriaResp resp;
		
		try {
			LOG.debug("id: " + id);
			
			resp = menuService.getImage(id);
			
			LOG.debug(resp);
		} catch (Exception e) {
			resp = new GetImageCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}

}
