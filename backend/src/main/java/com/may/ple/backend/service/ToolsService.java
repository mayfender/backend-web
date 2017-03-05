package com.may.ple.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.stereotype.Service;

import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.POIExcelUtil;

@Service
public class ToolsService {
	private static final Logger LOG = Logger.getLogger(ToolsService.class.getName());
	
	public ByteArrayOutputStream excel2txt(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, FileDetail fd) throws Exception {
		OutputStreamWriter writer = null;
		
		try {
			LOG.debug("File ext: " + fd.fileExt);
			if(!fd.fileExt.equals(".xlsx")) throw new CustomerException(5000, "File Type not support");
			
			Workbook workbook = new XSSFWorkbook(uploadedInputStream);
			
			Sheet sheet = workbook.getSheetAt(0);
			POIExcelUtil.removeSheetExcept0(workbook);
			
			ByteArrayOutputStream outputArray = new ByteArrayOutputStream();
			writer = new OutputStreamWriter(outputArray);
			StringBuilder txtRaw = new StringBuilder();
			int cellIndex;
			int countNull;
			Cell cell;
			int r = 0;
			Row row;
			
			while(true) {
				if(r > 0) {
					txtRaw.deleteCharAt(txtRaw.length() - 1);
					txtRaw.append(System.getProperty("line.separator"));
				}
				
				row = sheet.getRow(r++);
				if(row == null) {
					r--;
					break;
				}
				
				cellIndex = 0;
				countNull = 0;
				
				while(true) {
					cell = row.getCell(cellIndex++, MissingCellPolicy.RETURN_BLANK_AS_NULL);				
					
					if(countNull == 10) break;
					
					if(cell == null || StringUtils.isBlank(String.valueOf(cell))) {
						countNull++;
						continue;
					} else {
						countNull = 0;
						
						txtRaw.append(new DataFormatter(Locale.ENGLISH).formatCellValue(cell) + "|");
					}
				}			
			}
			
			writer.write(txtRaw.toString());
			
			return outputArray;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(writer != null) writer.close();
		}
	}
	
}
