package com.may.ple.backend.excel;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RegisterReport {
	private static final Logger LOG = Logger.getLogger(RegisterReport.class.getName());	
	private ByteArrayOutputStream out;
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private int startRow = 2;
	
	public byte[] proceed(Long id, String path) throws Exception {
		LOG.debug("Start");
		try {
			out = new ByteArrayOutputStream();
			workbook = new XSSFWorkbook(new FileInputStream(path));
			sheet = workbook.getSheetAt(0);
			
			
			drawMenuType("Mayfender");
			
			
			//----------------------------------------------------
			workbook.write(out);
			LOG.debug("End");
			return out.toByteArray();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try {if(workbook != null) workbook.close();} catch (Exception e2) {}
			try {if(out != null) out.close();} catch (Exception e2) {}
		}
	}

	public void drawMenuType(String name) {
		XSSFRow row = sheet.createRow(startRow);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue(name);
		startRow++;
	}

	public void drawSubMenuType(String name) {
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		
		XSSFRow row = sheet.createRow(startRow);
		XSSFCell cell = row.createCell(0);
		cell.setCellStyle(cellStyle);
		cell.setCellValue(name);
		startRow++;
	}
/*
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
	}*/

	
}
