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

public class WebReport1Impl extends ExcelGenerator {
	
	public WebReport1Impl(String sheetName) {
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
	    
	    sheet.setColumnWidth(index, 1200);
	    Cell cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("#");
	    
	    sheet.setColumnWidth(index, 4000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("เลขบัตรประชาชน");
	    
	    sheet.setColumnWidth(index, 5000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("ชื่อ-สกุล");
	    
	    sheet.setColumnWidth(index, 5000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("สถานภาพบุคคล");
	    
	    sheet.setColumnWidth(index, 3500);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("เดือนปีเกิด");
	    
	    sheet.setColumnWidth(index, 2000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("เพศ");
	    
	    sheet.setColumnWidth(index, 8000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("ข้อมูล ณ วันที่");
	    
	    sheet.setColumnWidth(index, 10000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("สิทธิที่ใช้เบิก");
	    
	    sheet.setColumnWidth(index, 4000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("วันที่ออกบัตร");
	    
	    sheet.setColumnWidth(index, 4000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("วันบัตรหมดอายุ");
	    
	    sheet.setColumnWidth(index, 6000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("จังหวัดที่ลงทะเบียนรักษา");
	    
	    sheet.setColumnWidth(index, 6000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("รพ. รักษา(ประกันสังคม)");
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
				cell.setCellValue(map.get("status"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("birthDate"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("gender"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("dataDate"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("right"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("issuedDate"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("expiredDate"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("province"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("hospital"));
			}
		} catch (Exception e) {
			Log.error(e.toString());
			throw e;
		}
	}

}
