package com.may.ple.backend;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestPOI {
	
	public static void main(String[] args) {
		String path = "C:\\Users\\mayfender\\Desktop\\New folder\\20171223131245204\\";
		
		try (
			XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(path + "20171223131245204.xlsx"));
			FileOutputStream fileOut = new FileOutputStream(path + "out.xlsx");
		){
			
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow r0 = sheet.getRow(0);
			XSSFRow r1 = sheet.getRow(1);
			XSSFCell c0 = r0.getCell(0);
			XSSFCell c, c4, c5;
			int firstWidth = 0, secondWidth = 0;
			
			for (int i = 0; i < 10; i++) {
				//-------------------------: Row 0 :--------------------------------
				firstWidth = 4 + (i*2);
				secondWidth = 5 + (i*2);
				
				sheet.setColumnWidth(firstWidth, 4000);
				sheet.setColumnWidth(secondWidth, 4000);
				
				c4 = r0.createCell(firstWidth);
				c5 = r0.createCell(secondWidth);
				
				c4.copyCellFrom(c0, new CellCopyPolicy());
				c5.copyCellFrom(c0, new CellCopyPolicy());
				c4.setCellValue("ชำระครั้งที่ " + (i + 1));
				
				sheet.addMergedRegion(new CellRangeAddress(
			            0, //first row (0-based)
			            0, //last row  (0-based)
			            firstWidth, //first column (0-based)
			            secondWidth  //last column  (0-based)
			    ));
				
				//-------------------------: Row 1 :--------------------------------
				c4 = r1.createCell(firstWidth);
				c5 = r1.createCell(secondWidth);
				
				c4.copyCellFrom(c0, new CellCopyPolicy());
				c5.copyCellFrom(c0, new CellCopyPolicy());
				c4.setCellValue("จำนวนเงินที่ชำระ");
				c5.setCellValue("วันที่ชำระ");
			}
			
			if(secondWidth != 0) {
				sheet.setColumnWidth(++secondWidth, 4000);
				c = r0.createCell(secondWidth);
				c.copyCellFrom(c0, new CellCopyPolicy());
				c.setCellValue("รวมเงินที่ชำระ");
				
				c = r1.createCell(secondWidth);
				c.copyCellFrom(c0, new CellCopyPolicy());
				
				sheet.addMergedRegion(new CellRangeAddress(
			            0, //first row (0-based)
			            1, //last row  (0-based)
			            secondWidth, //first column (0-based)
			            secondWidth  //last column  (0-based)
			    ));
				
				//[*]
				sheet.setColumnWidth(++secondWidth, 7000);
				c = r0.createCell(secondWidth);
				c.copyCellFrom(c0, new CellCopyPolicy());
				c.setCellValue("พนักงาน");
				
				c = r1.createCell(secondWidth);
				c.copyCellFrom(c0, new CellCopyPolicy());
				
				sheet.addMergedRegion(new CellRangeAddress(
			            0, //first row (0-based)
			            1, //last row  (0-based)
			            secondWidth, //first column (0-based)
			            secondWidth  //last column  (0-based)
			    ));
			}
			
			
			
			
			workbook.write(fileOut);
			System.out.println("finished");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
