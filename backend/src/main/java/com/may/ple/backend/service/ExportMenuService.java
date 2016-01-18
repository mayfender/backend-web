package com.may.ple.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
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
	@Value("${ext.template.menu}")
	private String menuTemplatePath;
	
	@Autowired
	public ExportMenuService(LoadDataService loadDataService, SubMenuService subMenuService) {
		this.loadDataService = loadDataService;
		this.subMenuService = subMenuService;
	}
	
	public byte[] exportMenu() throws Exception {		
		XWPFDocument document = null;
		ByteArrayOutputStream out = null;
			
		try {
			LOG.debug("Start");
			out = new ByteArrayOutputStream();
			document = new XWPFDocument(new FileInputStream(menuTemplatePath)); 
			
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
	
	private void drawMenuType(XWPFTable table, String name) {
		
		boolean isBold = true;
		int fontSize = 16;
		
		XWPFTableRow row = table.createRow();	
		XWPFTableCell cell = row.getCell(0);
		XWPFParagraph para = cell.getParagraphs().get(0);
		XWPFRun rh = para.createRun();
		
		rh.setFontSize(fontSize);
		rh.setBold(isBold);
		rh.setFontFamily("Angsana New");
		rh.setText(name);
		
		cell = row.createCell();
		cell.setText("");
		
		cell = row.createCell();
		cell.setText("");
	}
	
	private void drawSubMenuType(XWPFTable table, String name) {
		
		boolean isBold = true;
		int fontSize = 16;
		
		XWPFTableRow row = table.createRow();	
		XWPFTableCell cell = row.getCell(0);
		XWPFParagraph para = cell.getParagraphs().get(0);
		para.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun rh = para.createRun();
		
		rh.setFontSize(fontSize);
		rh.setBold(isBold);
		rh.setFontFamily("Angsana New");
		rh.setText(name);
		
		cell = row.createCell();
		cell.setText("");
		
		cell = row.createCell();
		cell.setText("");
	}
	
	private void drawMenu(XWPFTable table, Menu menu, List<SubMenu> subMenus, int index) {
		
		boolean isBold = false;
		int fontSize = 14;
		String subMenuTxt = "";
		
		for (SubMenu subMenu : subMenus) {
			subMenuTxt += ", " + subMenu.getName() + " " + String.format("%,.2f-", subMenu.getPrice());
		}
		
		if(subMenuTxt.length() > 0) {
			subMenuTxt = subMenuTxt.substring(1);			
		}
		
		XWPFTableRow row = table.createRow();	
		XWPFTableCell cell = row.getCell(0);
		XWPFParagraph para = cell.getParagraphs().get(0);
		para.setAlignment(ParagraphAlignment.LEFT);
		XWPFRun rh = para.createRun();
		
		rh.setFontSize(fontSize);
		rh.setBold(isBold);
		rh.setFontFamily("Angsana New");
		rh.setText(" " + (index + 1) + ". " + menu.getName() + "  " + subMenuTxt);
		
		cell = row.createCell();
		para = cell.getParagraphs().get(0);
		para.setAlignment(ParagraphAlignment.CENTER);
		rh = para.createRun();
		rh.setFontSize(fontSize);
		rh.setBold(isBold);
		rh.setFontFamily("Angsana New");
		rh.setText("");
		
		cell = row.createCell();
		para = cell.getParagraphs().get(0);
		para.setAlignment(ParagraphAlignment.RIGHT);
		rh = para.createRun();
		rh.setFontSize(fontSize);
		rh.setBold(isBold);
		rh.setFontFamily("Angsana New");
		rh.setText(String.format("%,.2f-", menu.getPrice()));
	}
	
}