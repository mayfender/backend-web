package com.may.ple.backend.utils;

import java.io.FileOutputStream;
import java.util.Calendar;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelTemplateUtil {
	
	private static CellStyle hSty1(Workbook workbook, Font font) {
		CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND); 
        style.setBorderTop((short)1);
        style.setBorderRight((short)1);
        style.setBorderBottom((short)1);
        style.setBorderLeft((short)1);
        return style;
	}
	
	private static CellStyle dateSty1(Workbook workbook, CreationHelper helper, CellStyle basedStyle) {
		if(basedStyle == null) {
			basedStyle = workbook.createCellStyle();
		}
		basedStyle.setDataFormat(helper.createDataFormat().getFormat("dd/MM/yyyy"));
		return basedStyle;
	}
	
	private static Font font1(Workbook workbook) {
		Font font = workbook.createFont();
		font.setBold(true);
		font.setFontName("Angsana New");
		font.setFontHeightInPoints((short) 14);
		return font;
	}
	private static Font font2(Workbook workbook) {        
		Font font = workbook.createFont();
		font.setFontName("Angsana New");
		font.setFontHeightInPoints((short) 14);
		return font;
	}
	
	public static void start() {
		XSSFWorkbook workbook = null;
		FileOutputStream fileOut = null;
		try {
			System.out.println("going.");
			
			workbook = new XSSFWorkbook();
			CreationHelper createHelper = workbook.getCreationHelper();
			
	        //---[1]
	        CellStyle hSty = hSty1(workbook, font1(workbook));
	        
	        //---[2]
	        CellStyle bodyCellStyle = workbook.createCellStyle();
	        bodyCellStyle.setFont(font2(workbook));
	        
	        //---[3]
			XSSFSheet sheet = workbook.createSheet("Data");
			XSSFRow row = sheet.createRow(0);
			XSSFCell cell;
			
			for (int i = 0; i < 6; i++) {
				cell = row.createCell(i);
				cell.setCellValue("Cell-" + i);
				cell.setCellStyle(hSty);
			}
			
			for (int i = 1; i < 10; i++) {
				row = sheet.createRow(i);
				
				for (int j = 0; j < 6; j++) {
					cell = row.createCell(j);
					
					if(j < 5) {
						cell.setCellValue("data-");
						cell.setCellStyle(bodyCellStyle);						
					} else {
						cell.setCellValue(Calendar.getInstance().getTime());
						cell.setCellStyle(dateSty1(workbook, createHelper, bodyCellStyle));						
					}
				}
			}
			
			// Write the output to a file
	        fileOut = new FileOutputStream("D://Print-Report.xlsx");
	        workbook.write(fileOut);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if(fileOut != null) fileOut.close(); } catch (Exception e2) {}
			try { if(workbook != null) workbook.close(); } catch (Exception e2) {}
		}
	}
	
}
