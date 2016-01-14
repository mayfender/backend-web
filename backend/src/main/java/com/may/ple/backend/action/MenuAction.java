package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.EditMenuDataCriteriaResp;
import com.may.ple.backend.criteria.GetImageCriteriaResp;
import com.may.ple.backend.criteria.MenuDetailHtmlCriteriaResp;
import com.may.ple.backend.criteria.MenuSaveCriteriaReq;
import com.may.ple.backend.criteria.MenuSaveCriteriaResp;
import com.may.ple.backend.criteria.MenuSearchCriteriaReq;
import com.may.ple.backend.criteria.MenuSearchCriteriaResp;
import com.may.ple.backend.entity.MenuType;
import com.may.ple.backend.service.MenuService;
import com.may.ple.backend.service.MenuTypeService;

@Component
@Path("menu")
public class MenuAction {
	private static final Logger LOG = Logger.getLogger(MenuAction.class.getName());
	private MenuService menuService;
	private MenuTypeService menuTypeService;
	
	@Autowired
	public MenuAction(MenuService menuService, MenuTypeService menuTypeService) {
		this.menuService = menuService;
		this.menuTypeService = menuTypeService;
	}
	
	@POST
	@Path("/searchMenu")
	public MenuSearchCriteriaResp searchMenu(MenuSearchCriteriaReq req) {
		LOG.debug("Start");
		MenuSearchCriteriaResp resp;
		
		try {
			LOG.debug(req);
			
			resp = menuService.searchMenu(req);
			
			resp.setMenuTypes(menuTypeService.loadMenuType());
		} catch (Exception e) {
			resp = new MenuSearchCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/saveMenu")
	public MenuSaveCriteriaResp saveMenu(MenuSaveCriteriaReq req) {
		LOG.debug("Start");
		MenuSaveCriteriaResp resp = new MenuSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			
			menuService.saveMenu(req);			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateMenu")
	public MenuSaveCriteriaResp updateMenu(MenuSaveCriteriaReq req) {
		LOG.debug("Start");
		MenuSaveCriteriaResp resp = new MenuSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			
			menuService.updateMenu(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/deleteMenu")
	@Secured("ROLE_ADMIN")
	public MenuSearchCriteriaResp deleteMenu(MenuSearchCriteriaReq req) {
		LOG.debug("Start");
		MenuSearchCriteriaResp resp;
		
		try {
			LOG.debug(req);
			menuService.deleteMenu(req.getId());
			
			resp = searchMenu(req);
		} catch (Exception e) {
			resp = new MenuSearchCriteriaResp(1000);
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
		} catch (Exception e) {
			resp = new GetImageCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/editData")
	public EditMenuDataCriteriaResp editData(@QueryParam("imgId") Long imgId, @QueryParam("menuTypeId") Long menuTypeId) {
		LOG.debug("Start");
		EditMenuDataCriteriaResp resp = new EditMenuDataCriteriaResp();
		
		try {
			LOG.debug("imgId: " + imgId + ", menuTypeId: " + menuTypeId);
			
			if(imgId != null) {
				GetImageCriteriaResp imageCriteriaResp = menuService.getImage(imgId);
				resp.setImgBase64(imageCriteriaResp.getImgBase64());				
			}
			
			List<MenuType> menuTypeChilds = menuTypeService.getMenuTypeChilds(menuTypeId);
			resp.setMenuTypeChilds(menuTypeChilds);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getMenuDetailHtml")
	public MenuDetailHtmlCriteriaResp getMenuDetailHtml(@QueryParam("id") long id) {
		LOG.debug("Start");
		MenuDetailHtmlCriteriaResp resp = new MenuDetailHtmlCriteriaResp();
		
		try {
			LOG.debug("id: " + id);
			
			String html = menuService.getMenuDetailHtml(id);
			resp.setHtml(html);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

}
