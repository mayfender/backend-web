package com.may.ple.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
	@Value("${ext.template.menu.doc}")
	private String menuTemplateDocPath;
	@Value("${ext.template.menu.excel}")
	private String menuTemplateExcelPath;
	
	@Autowired
	public ExportMenuService(LoadDataService loadDataService, SubMenuService subMenuService, @Qualifier("docDrawMenuService")DrawMenu drawMenu) {
		this.loadDataService = loadDataService;
		this.subMenuService = subMenuService;
		this.drawMenu = drawMenu;
	}
	
	/*public static void main(String[] args) {
		try {
			exportMenuExcel();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	/*public void exportMenuExcel() throws Exception {		
		FileOutputStream out = null;
		XSSFWorkbook workbook = null;
		
		try {
			workbook = new XSSFWorkbook(new FileInputStream(menuTemplateExcelPath));
			XSSFSheet sheet = workbook.getSheet("menu");
//			XSSFRow row = sheet.createRow(2);
//			XSSFCell cell = row.createCell(0);
//			cell.setCellValue("testing");
			
			
			
			
			GetMenuTypeCriteriaResp resp = loadDataService.getMenuType();
			Map<String, List<MenuType>> menuTypesMap = resp.getMenuTypesMap();
			Map<String, List<Menu>> menusMap = resp.getMenusMap();
			
			Set<String> menuTypeKey = menuTypesMap.keySet();
			List<MenuType> menuTypes;
			List<Menu> menus;
			List<SubMenu> subMenus;
			Menu menu;
			
			for (String key : menuTypeKey) {
				LOG.debug("menuType: " + key);
				drawMenuType(table, key);
				
				menuTypes = menuTypesMap.get(key);
				
				for (MenuType menuType : menuTypes) {
					drawSubMenuType(table, menuType.getName());
					
					menus = loadDataService.getMenus(menuType.getId());
					
					for (int i = 0; i < menus.size(); i++) {
						menu = menus.get(i);
						subMenus = subMenuService.findByMenuId(menu.getId());
						
						drawMenu(table, menu, subMenus, i);
					}
				}
				
				if(menuTypes.size() == 0) {
					LOG.debug("Not found subMenuType");
					menus = menusMap.get(key);
					
					for (int i = 0; i < menus.size(); i++) {
						menu = menus.get(i);
						subMenus = subMenuService.findByMenuId(menu.getId());
						
						drawMenu(table, menu, subMenus, i);						
					}
				}
			}
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			out = new FileOutputStream(new File("D:\\templates\\test.xlsx"));
			workbook.write(out);
			
			System.out.println("finished");
		} catch (Exception e) {
			throw e;
		} finally {
			workbook.close();
			out.close();			
		}
	}*/
	
	public byte[] exportMenuWord() throws Exception {		
		XWPFDocument document = null;
		ByteArrayOutputStream out = null;
			
		try {
			LOG.debug("Start");
			out = new ByteArrayOutputStream();
			document = new XWPFDocument(new FileInputStream(menuTemplateDocPath)); 
			
			List<XWPFTable> tables = document.getTables();
			XWPFTable table = tables.get(0);
			
			GetMenuTypeCriteriaResp resp = loadDataService.getMenuType();
			Map<String, List<MenuType>> menuTypesMap = resp.getMenuTypesMap();
			Map<String, List<Menu>> menusMap = resp.getMenusMap();
			
			Set<String> menuTypeKey = menuTypesMap.keySet();
			List<MenuType> menuTypes;
			List<Menu> menus;
			List<SubMenu> subMenus;
			Menu menu;
			
			for (String key : menuTypeKey) {
				LOG.debug("menuType: " + key);
				drawMenu.drawMenuType(table, key);
				
				menuTypes = menuTypesMap.get(key);
				
				for (MenuType menuType : menuTypes) {
					drawMenu.drawSubMenuType(table, menuType.getName());
					
					menus = loadDataService.getMenus(menuType.getId());
					
					for (int i = 0; i < menus.size(); i++) {
						menu = menus.get(i);
						subMenus = subMenuService.findByMenuId(menu.getId());
						
						drawMenu.drawMenu(table, menu, subMenus, i);
					}
				}
				
				if(menuTypes.size() == 0) {
					LOG.debug("Not found subMenuType");
					menus = menusMap.get(key);
					
					for (int i = 0; i < menus.size(); i++) {
						menu = menus.get(i);
						subMenus = subMenuService.findByMenuId(menu.getId());
						
						drawMenu.drawMenu(table, menu, subMenus, i);
					}
				}
			}
			
			document.write(out);
			LOG.debug("End");
			
			return out.toByteArray();
		} catch (Exception e) {
			throw e;
		} finally {
			if(document != null) document.close();
			if(out != null) out.close();
		}
	}
	
}