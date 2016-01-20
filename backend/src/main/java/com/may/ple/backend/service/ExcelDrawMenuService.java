package com.may.ple.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.SubMenu;

@Service
public class ExcelDrawMenuService implements DrawMenu {
	private static final Logger LOG = Logger.getLogger(ExcelDrawMenuService.class.getName());
	@Value("${ext.template.menu.excel}")
	private String menuTemplateExcelPath;
	private ByteArrayOutputStream out;
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private int startRow = 2;
	
	@Override
	public void init() {
		try {
			out = new ByteArrayOutputStream();
			workbook = new XSSFWorkbook(new FileInputStream(menuTemplateExcelPath));
			sheet = workbook.getSheet("menu");
		} catch (Exception e) {
			LOG.error(e.toString());
		}
	}

	@Override
	public void drawMenuType(String name) {
		XSSFRow row = sheet.createRow(startRow);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue(name);
		startRow++;
	}

	@Override
	public void drawSubMenuType(String name) {
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		
		XSSFRow row = sheet.createRow(startRow);
		XSSFCell cell = row.createCell(0);
		cell.setCellStyle(cellStyle);
		cell.setCellValue(name);
		startRow++;
	}

	@Override
	public void drawMenu(Menu menu, List<SubMenu> subMenus, int index) {
		String subMenuTxt = "";
		
		for (SubMenu subMenu : subMenus) {
			subMenuTxt += ", " + subMenu.getName() + " " + String.format("%,.2f-", subMenu.getPrice());
		}
		
		if(subMenuTxt.length() > 0) {
			subMenuTxt = subMenuTxt.substring(1);			
		}
		
		XSSFRow row = sheet.createRow(startRow);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue(" " + (index + 1) + ". " + menu.getName() + "  " + subMenuTxt);
		
		cell = row.createCell(2);
		cell.setCellValue(String.format("%,.2f-", menu.getPrice()));
		
		startRow++;
	}

	@Override
	public byte[] getByte() {
		byte[] result = null;
		
		try {
			
			workbook.write(out);
			result = out.toByteArray();
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
		} finally {
			try {if(workbook != null) workbook.close();} catch (Exception e2) {}
			try {if(out != null) out.close();} catch (Exception e2) {}
			
			out = null;
			workbook = null;
			sheet = null;
			startRow = 2;
		}
		return result;
	}

}
