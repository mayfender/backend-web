package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.ImportMenuDeleteCriteriaReq;
import com.may.ple.backend.criteria.ImportMenuFindCriteriaReq;
import com.may.ple.backend.criteria.ImportMenuFindCriteriaResp;
import com.may.ple.backend.criteria.ImportMenuSaveCriteriaReq;
import com.may.ple.backend.criteria.ImportMenuSaveCriteriaResp;
import com.may.ple.backend.entity.ImportMenu;
import com.may.ple.backend.service.ImportMenuService;

@Component
@Path("importMenu")
public class ImportMenuAction {
	private static final Logger LOG = Logger.getLogger(ImportMenuAction.class.getName());
	private ImportMenuService service;
	
	@Autowired
	public ImportMenuAction(ImportMenuService service) {
		this.service = service;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public ImportMenuFindCriteriaResp find(ImportMenuFindCriteriaReq req) {
		LOG.debug("Start");
		ImportMenuFindCriteriaResp resp = new ImportMenuFindCriteriaResp();
		
		try {
			LOG.debug(req);
			List<ImportMenu> menus = service.find(req);
			resp.setMenus(menus);;
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
	@Produces(MediaType.APPLICATION_JSON)
	public ImportMenuSaveCriteriaResp save(ImportMenuSaveCriteriaReq req) {
		LOG.debug("Start");
		ImportMenuSaveCriteriaResp resp = new ImportMenuSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String menuId = service.save(req);
			resp.setMenuId(menuId);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp delete(ImportMenuDeleteCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug(req);
			service.delete(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
		
}
