package com.may.ple.backend.bussiness;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class ExcelGenerator {
	protected Workbook wb;
	protected Sheet sheet;
	
	public ExcelGenerator(String sheetName) {
		wb = new XSSFWorkbook();
		sheet = wb.createSheet(sheetName);
	}
	
	/**
	 * abstract method
	 */
	abstract protected void createHeader();
	abstract protected void createBody(List<Map<String, String>> data);
	
	public void createReport(List<Map<String, String>> data, OutputStream out) {
		// header
		createHeader();
		
		// body
		createBody(data);
		
		// write out
		write(out);
	}
	
	private void write(OutputStream out) {
		try {
			wb.write(out);
			out.close();
			wb.close();			
		} catch (Exception e) {
			
		}
	}

}
