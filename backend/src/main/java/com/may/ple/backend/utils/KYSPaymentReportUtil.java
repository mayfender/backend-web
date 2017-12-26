package com.may.ple.backend.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class KYSPaymentReportUtil {
	private static final Logger LOG = Logger.getLogger(KYSPaymentReportUtil.class.getName());
	
	public static void payInfoColumn(XSSFSheet sheet, int startIndex, int round) {
		try {
			XSSFRow r0 = sheet.getRow(0);
			XSSFRow r1 = sheet.getRow(1);
			XSSFRow r2 = sheet.getRow(2);
			XSSFCell r0c4 = r0.getCell(4);
			XSSFCell r2c4 = r2.getCell(4);
			XSSFCell r2c5 = r2.getCell(5);
			XSSFCell r0cf, r0cs, r2cf, r2cs;
			
			sheet.setColumnWidth(startIndex, 3600);
			sheet.setColumnWidth(startIndex + 1, 3600);
			
			r0cf = r0.createCell(startIndex);
			r0cs = r0.createCell(startIndex + 1);
			r0cf.copyCellFrom(r0c4, new CellCopyPolicy());
			r0cs.copyCellFrom(r0c4, new CellCopyPolicy());
			r0cf.setCellValue("ชำระครั้งที่ " + round);
			
			r2cf = r2.createCell(startIndex);
			r2cs = r2.createCell(startIndex + 1);
			r2cf.copyCellFrom(r2c4, new CellCopyPolicy());
			r2cf.setCellType(Cell.CELL_TYPE_BLANK);
			r2cs.copyCellFrom(r2c5, new CellCopyPolicy());
			r2cs.setCellType(Cell.CELL_TYPE_BLANK);
			
			sheet.addMergedRegion(new CellRangeAddress(
		            0, //first row (0-based)
		            0, //last row  (0-based)
		            startIndex, //first column (0-based)
		            startIndex + 1  //last column  (0-based)
		    ));
			
			r0cf = r1.createCell(startIndex);
			r0cs = r1.createCell(startIndex + 1);
			
			r0cf.copyCellFrom(r0c4, new CellCopyPolicy());
			r0cs.copyCellFrom(r0c4, new CellCopyPolicy());
			r0cf.setCellValue("จำนวนเงินที่ชำระ");
			r0cs.setCellValue("วันที่ชำระ");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public static void othersColumn(XSSFSheet sheet, int startIndex) {
		try {
			XSSFRow r0 = sheet.getRow(0);
			XSSFRow r1 = sheet.getRow(1);
			XSSFRow r2 = sheet.getRow(2);
			XSSFCell r0c0 = r0.getCell(0);
			XSSFCell r2c0 = r2.getCell(0);
			XSSFCell r2c4 = r2.getCell(4);
			XSSFCell c;
			
			sheet.setColumnWidth(startIndex, 4000);
			c = r0.createCell(startIndex);
			c.copyCellFrom(r0c0, new CellCopyPolicy());
			c.setCellValue("รวมเงินที่ชำระ");
			
			c = r1.createCell(startIndex);
			c.copyCellFrom(r0c0, new CellCopyPolicy());
			
			sheet.addMergedRegion(new CellRangeAddress(
		            0, //first row (0-based)
		            1, //last row  (0-based)
		            startIndex, //first column (0-based)
		            startIndex  //last column  (0-based)
		    ));
			
			c = r2.createCell(startIndex);
			String strFormula= "SUM(" + r2.getCell(4).getReference() + ":" + r2.getCell(startIndex - 1).getReference() + ")";
			c.setCellType(HSSFCell.CELL_TYPE_FORMULA);
			c.copyCellFrom(r2c4, new CellCopyPolicy());
			c.setCellFormula(strFormula);
			
			//[*]
			sheet.setColumnWidth(++startIndex, 7000);
			c = r0.createCell(startIndex);
			c.copyCellFrom(r0c0, new CellCopyPolicy());
			c.setCellValue("พนักงาน");
			
			c = r1.createCell(startIndex);
			c.copyCellFrom(r0c0, new CellCopyPolicy());
			
			c = r2.createCell(startIndex);
			c.copyCellFrom(r2c0, new CellCopyPolicy());
			c.setCellValue("${taskDetail.sys_owner}");
			
			sheet.addMergedRegion(new CellRangeAddress(
		            0, //first row (0-based)
		            1, //last row  (0-based)
		            startIndex, //first column (0-based)
		            startIndex  //last column  (0-based)
		    ));
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public static void main(String[] args) {
		String path = "C:\\Users\\sarawuti\\Desktop\\test\\";
		
		try (
			XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(path + "test.xlsx"));
			FileOutputStream fileOut = new FileOutputStream(path + "out.xlsx");
		){
			XSSFSheet sheet = workbook.getSheetAt(0);
			
			
			int startIndex = 6;
			int lastIndex = 0;
			for (int i = 0; i < 3; i++) {
				lastIndex = startIndex + (i * 2);
				
				payInfoColumn(sheet, lastIndex, i + 2);
			}
			if(lastIndex != 0) {
				othersColumn(sheet, lastIndex + 2);				
			}
			
			workbook.write(fileOut);
			workbook.close();
			
			System.out.println("finished");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}