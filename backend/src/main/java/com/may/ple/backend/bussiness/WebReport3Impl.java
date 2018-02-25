package com.may.ple.backend.bussiness;

import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.jfree.util.Log;

public class WebReport3Impl extends ExcelGenerator {
	
	public WebReport3Impl(String sheetName) {
		super(sheetName);
	}
	
	@Override
	protected void createHeader() {
		CellStyle cellStyle = wb.createCellStyle();
	    cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
	    cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
	    cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
	    cellStyle.setFillForegroundColor(HSSFColor.LIGHT_ORANGE.index);
	    cellStyle.setBorderRight(CellStyle.BORDER_THIN);
	    cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
	    cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
	    cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	    cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
	    cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	    cellStyle.setBorderTop(CellStyle.BORDER_THIN);
	    cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
	    
	    Row row = sheet.createRow(0);
	    row.setHeight((short)450);
	    int index = 0;
	    
	    sheet.setColumnWidth(index, 2000);
	    Cell cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("ลำดับที่");
	    
	    sheet.setColumnWidth(index, 4000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("เลขบัตรประชาชน");
	    
	    sheet.setColumnWidth(index, 5000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("Name");
	    
	    sheet.setColumnWidth(index, 12000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("Address");
	    
	    sheet.setColumnWidth(index, 4000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("Phone");
	    
	    sheet.setColumnWidth(index, 4000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("Office Phone");
	    
	    sheet.setColumnWidth(index, 4000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("Fax/Mobile");
	}

	@Override
	protected void createBody(List<Map<String, String>> data) {
		try {
			if(data == null) return;
			
			CellStyle cellStyle = wb.createCellStyle();
			cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			int rowIndex = 1;
			Row row;
			Cell cell;
			int index = 1, cellIndex;
			
			for (Map<String, String> map : data) {
				cellIndex = 0;
				
				row = sheet.createRow(rowIndex++);
				
				cell = row.createCell(cellIndex++);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(index++);
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("ID Number"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("name"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("addr"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("phone"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("phoneOffice"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("faxMobile"));
			}
		} catch (Exception e) {
			Log.error(e.toString());
			throw e;
		}
	}

}
