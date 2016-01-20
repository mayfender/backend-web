package com.may.ple.backend.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.may.ple.backend.constant.DocType;
import com.may.ple.backend.criteria.GetMenuTypeCriteriaResp;
import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.MenuType;
import com.may.ple.backend.entity.SubMenu;

@Service
public class ExportMenuService {
	private static final Logger LOG = Logger.getLogger(ExportMenuService.class.getName());
	private LoadDataService loadDataService;
	private SubMenuService subMenuService;
	private DrawMenu drawMenu;
	private DrawMenu drawMenuWord;
	private DrawMenu drawMenuExcel;
	
	@Autowired
	public ExportMenuService(LoadDataService loadDataService, SubMenuService subMenuService,
			@Qualifier("docDrawMenuService")DrawMenu drawMenuWord,
			@Qualifier("excelDrawMenuService")DrawMenu drawMenuExcel) {
		this.loadDataService = loadDataService;
		this.subMenuService = subMenuService;
		this.drawMenuWord = drawMenuWord;
		this.drawMenuExcel = drawMenuExcel;
	}
	
	public byte[] exportMenu(DocType docType) throws Exception {		
		try {
			LOG.debug("Start");
			
			GetMenuTypeCriteriaResp resp = loadDataService.getMenuType();
			Map<String, List<MenuType>> menuTypesMap = resp.getMenuTypesMap();
			Map<String, List<Menu>> menusMap = resp.getMenusMap();
			
			Set<String> menuTypeKey = menuTypesMap.keySet();
			List<MenuType> menuTypes;
			List<Menu> menus;
			List<SubMenu> subMenus;
			Menu menu;
			
			if(docType == DocType.EXCEL) {
				drawMenu = drawMenuExcel;
			} else if(docType == DocType.WORD) {
				drawMenu = drawMenuWord;
			}
			
			drawMenu.init();
			
			for (String key : menuTypeKey) {
				LOG.debug("menuType: " + key);
				drawMenu.drawMenuType(key);
				
				menuTypes = menuTypesMap.get(key);
				
				for (MenuType menuType : menuTypes) {
					drawMenu.drawSubMenuType(menuType.getName());
					
					menus = loadDataService.getMenus(menuType.getId());
					
					for (int i = 0; i < menus.size(); i++) {
						menu = menus.get(i);
						subMenus = subMenuService.findByMenuId(menu.getId());
						
						drawMenu.drawMenu(menu, subMenus, i);
					}
				}
				
				if(menuTypes.size() == 0) {
					LOG.debug("Not found subMenuType");
					menus = menusMap.get(key);
					
					for (int i = 0; i < menus.size(); i++) {
						menu = menus.get(i);
						subMenus = subMenuService.findByMenuId(menu.getId());
						
						drawMenu.drawMenu(menu, subMenus, i);
					}
				}
			}
			
			LOG.debug("End");
			
			return drawMenu.getByte();
		} catch (Exception e) {
			throw e;
		}
	}
	
}