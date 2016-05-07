package com.may.ple.backend.excel;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.may.ple.backend.entity.SptRegistration;

public class RegisterReport {
	private static final Logger LOG = Logger.getLogger(RegisterReport.class.getName());	
	private List<SptRegistration> registereds;
	private ByteArrayOutputStream out;
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private String path;
	
	/*public static void main(String[] args) {
		
		List<SptRegistration> registereds = new ArrayList<>();
		
		SptRegistration sptRegistration = new SptRegistration("memberId", null, "firstname", "lastname", "firstnameEng", "lastnameEng", 
				"citizenId", new Date(), "fingerId", new Date(), new Date(), "conTelNo", "conMobileNo1", 
				"conMobileNo2", "conMobileNo3", "conLineId", "conFacebook", "conEmail", "conAddress", null, 
				null, null, null, null, null, null, null, null);
		registereds.add(sptRegistration);
		
		sptRegistration = new SptRegistration("memberId2", null, "firstname2", "lastname2", "firstnameEng2", "lastnameEng2", 
				"citizenId2", new Date(), "fingerId2", new Date(), new Date(), "conTelNo-2", "conMobileNo1-2", 
				"conMobileNo2-2", "conMobileNo3-2", "conLineId2", "conFacebook2", "conEmail2", "conAddress2", null, 
				null, null, null, null, null, null, null, null);
		registereds.add(sptRegistration);
		
		sptRegistration = new SptRegistration("memberId2", null, "firstname2", "lastname2", "firstnameEng2", "lastnameEng2", 
				"citizenId2", new Date(), "fingerId2", new Date(), new Date(), "conTelNo-2", "conMobileNo1-2", 
				"conMobileNo2-2", "conMobileNo3-2", "conLineId2", "conFacebook2", "conEmail2", "conAddress2", null, 
				null, null, null, null, null, null, null, null);
		registereds.add(sptRegistration);
		
		sptRegistration = new SptRegistration("memberId2", null, "firstname2", "lastname2", "firstnameEng2", "lastnameEng2", 
				"citizenId2", new Date(), "fingerId2", new Date(), new Date(), "conTelNo-2", "conMobileNo1-2", 
				"conMobileNo2-2", "conMobileNo3-2", "conLineId2", "conFacebook2", "conEmail2", "conAddress2", null, 
				null, null, null, null, null, null, null, null);
		registereds.add(sptRegistration);
		
		sptRegistration = new SptRegistration("memberId2", null, "firstname2", "lastname2", "firstnameEng2", "lastnameEng2", 
				"citizenId2", new Date(), "fingerId2", new Date(), new Date(), "conTelNo-2", "conMobileNo1-2", 
				"conMobileNo2-2", "conMobileNo3-2", "conLineId2", "conFacebook2", "conEmail2", "conAddress2", null, 
				null, null, null, null, null, null, null, null);
		registereds.add(sptRegistration);
		
		FileOutputStream out = null;
		try {
			byte[] bytes = new RegisterReport(registereds, "D:/templates/registered_report.xlsx").proceed();
			
			out = new FileOutputStream("C:\\Users\\mayfender\\Desktop\\test.xlsx");
			out.write(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();				
			} catch (Exception e2) {
			}
		}
	}*/
	
	public RegisterReport(List<SptRegistration> registereds, String path) {
		this.registereds = registereds;
		this.path = path;
	}
	
	public byte[] proceed() throws Exception {
		LOG.debug("Start");
		try {
			out = new ByteArrayOutputStream();
			workbook = new XSSFWorkbook(new FileInputStream(path));
			sheet = workbook.getSheetAt(0);
			
			
			drawMenuType();
			
			
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

	public void drawMenuType() {
		
		if(registereds == null) return;
		
//		XSSFRow row = sheet.getRow(startRow);
//		XSSFCell cell = row.getCell(2);
//		cell.setCellValue(cell.getStringCellValue() + " " + "Mayfender");
//		startRow += 51
		
		int size = registereds.size();
		int startCopyRow = 0;
		int endCopyRow = 0;
		
		for (int i = 1; i <= size; i++) {
			if(i < size) {				
				sheet.copyRows(startCopyRow, endCopyRow = startCopyRow + 49, endCopyRow + 2, new CellCopyPolicy());
			} else {
				
			}
			
			startCopyRow = endCopyRow + 1;
//			SptRegistration registration = registereds.get(i);
		}
		
		
	}

	/*public void drawSubMenuType(String name) {
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		
		XSSFRow row = sheet.createRow(startRow);
		XSSFCell cell = row.createCell(0);
		cell.setCellStyle(cellStyle);
		cell.setCellValue(name);
		startRow++;
	}*/
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
