package com.may.ple.backend.bussiness;

import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.jfree.util.Log;

public class WebReport2Impl extends ExcelGenerator {
	
	public WebReport2Impl(String sheetName) {
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
	    
	    sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
	    sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 4));
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 5, 7));
	    sheet.addMergedRegion(new CellRangeAddress(0, 0, 8, 10));
	    
	    Row row = sheet.createRow(0);
	    row.setHeight((short)350);
	    
	    sheet.setColumnWidth(0, 2000);
	    Cell cell = row.createCell(0);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("ลำดับที่");
	    
	    sheet.setColumnWidth(1, 4000);
	    cell = row.createCell(1);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("เลขบัตรประชาชน");
	    
	    cell = row.createCell(2);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("หน่วยงานนายทะเบียนผู้รับบำเหน็จบำนาญ");
	    
	    cell = row.createCell(5);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("ส่วนราชการผู้ขอ");
	    
	    cell = row.createCell(8);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("ส่วนราชการผู้ขอ");
	    
	    //--------------------------------------------
	    row = sheet.createRow(1);
	    row.setHeight((short)350);
	    int index = 0;
	    
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    
	    sheet.setColumnWidth(index, 7000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("ส่วนราชการ");
	    
	    sheet.setColumnWidth(index, 5000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("จังหวัด");
	    
	    sheet.setColumnWidth(index, 10000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("หน่วยงานนายทะเบียนผู้รับบำเหน็จบำนาญ");
	    
	    sheet.setColumnWidth(index, 7000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("ส่วนราชการ");
	    
	    sheet.setColumnWidth(index, 5000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("จังหวัด");
	    
	    sheet.setColumnWidth(index, 10000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("ส่วนราชการผู้ขอ");
	    
	    sheet.setColumnWidth(index, 7000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("ส่วนราชการ");
	    
	    sheet.setColumnWidth(index, 5000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("จังหวัด");
	    
	    sheet.setColumnWidth(index, 10000);
	    cell = row.createCell(index++);
	    cell.setCellStyle(cellStyle);
	    cell.setCellValue("ส่วนราชการผู้เบิก");
	}

	@Override
	protected void createBody(List<Map<String, String>> data) {
		try {
			if(data == null) return;
			
			CellStyle cellStyle = wb.createCellStyle();
			cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			int rowIndex = 2;
			Row row;
			Cell cell;
			int index = 1, cellIndex;
			
			for (Map<String, String> map : data) {
				cellIndex = 0;
				
				row = sheet.createRow(rowIndex++);
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(index++);
				cell.setCellStyle(cellStyle);
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("ID Number"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("data_1_1"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("data_1_2"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("data_1_3"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("data_2_1"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("data_2_2"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("data_2_3"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("data_3_1"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("data_3_2"));
				
				cell = row.createCell(cellIndex++);
				cell.setCellValue(map.get("data_3_3"));
			}
		} catch (Exception e) {
			Log.error(e.toString());
			throw e;
		}
	}

}
