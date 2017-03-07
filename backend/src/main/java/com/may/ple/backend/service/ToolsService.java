package com.may.ple.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;

@Service
public class ToolsService {
	private static final Logger LOG = Logger.getLogger(ToolsService.class.getName());
	
	public ByteArrayOutputStream excel2txt(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, FileDetail fd) throws Exception {
		OutputStreamWriter writer = null;
		Workbook workbook = null;
		
		try {
			LOG.debug("File ext: " + fd.fileExt);						
			
			if(fd.fileExt.equals(".xlsx")) {
				workbook = new XSSFWorkbook(uploadedInputStream);
			} else if(fd.fileExt.equals(".xls")) {
				workbook = new HSSFWorkbook(uploadedInputStream);
			} else {
				throw new CustomerException(5000, "Filetype not match");
			}
			
			Sheet sheet = workbook.getSheetAt(0);
			List<ColumnFormat> columnFormats = new ArrayList<>();
			Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet, columnFormats);
			if(headerIndex.size() == 0) {		
				LOG.error("Not found Headers");
				return null;
			}
			
			ByteArrayOutputStream outputArray = new ByteArrayOutputStream();
			writer = new OutputStreamWriter(outputArray);
			StringBuilder txtRaw = new StringBuilder();
			Set<String> keySet = headerIndex.keySet();
			Cell cell;
			int r = 1;
			Row row;
			
			while(true) {
				if(r > 1) {
					if(txtRaw.length() != 0) {
						txtRaw.deleteCharAt(txtRaw.length() - 1);
						txtRaw.append(System.getProperty("line.separator"));
					}
				}
				
				row = sheet.getRow(r++);
				if(row == null) {
					r--;
					break;
				}
				
				for (String key : keySet) {
					cell = row.getCell(headerIndex.get(key), MissingCellPolicy.RETURN_BLANK_AS_NULL);
					
					if(cell == null || StringUtils.isBlank(String.valueOf(cell))) {
						txtRaw.append("|");
					} else {						
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
			if(workbook != null) workbook.close();
			if(writer != null) writer.close();
		}
	}
	
}
