package com.may.ple.backend.action;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
	
	@GET
	@Path("/loadAllMenu")
	public MenuCriteriaResp loadAllMenu() {
		LOG.debug("Start");
		MenuCriteriaResp resp = new MenuCriteriaResp();
		
		try {
			
			resp.setMenus(menuService.loadAllMenu());
			
			LOG.debug(resp);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString());
		}
		
		LOG.debug("End");
		return resp;
	}

}
