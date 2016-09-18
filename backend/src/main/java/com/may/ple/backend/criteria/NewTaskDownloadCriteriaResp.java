package com.may.ple.backend.criteria;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;

public class NewTaskDownloadCriteriaResp extends CommonCriteriaResp implements StreamingOutput {
	private static final Logger LOG = Logger.getLogger(NewTaskDownloadCriteriaResp.class.getName());
	private String filePath;
	private Boolean isCheckData;
	private List<Map> taskDetails;
	
	@Override
	public void write(OutputStream os) throws IOException, WebApplicationException {
		OutputStream out = null;
		ByteArrayInputStream in = null;
		FileInputStream fis = null;
		Workbook workbook = null;
		
		try {
			byte[] data;
			out = new BufferedOutputStream(os);
			
			if(isCheckData) {
				if(filePath.endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(new FileInputStream(filePath));
				} else if(filePath.endsWith(".xls")) {
					workbook = new HSSFWorkbook(new FileInputStream(filePath));
				} else {
					throw new CustomerException(5000, "Filetype not match");
				}
				
				workbook.setSheetName(0, workbook.getSheetName(0) + "_Validation");
				Sheet sheet = workbook.getSheetAt(0);
				int sheetIndex = 1;
				
				try {
					while(workbook.getSheetAt(sheetIndex) != null) {
						workbook.removeSheetAt(sheetIndex);
					}
				} catch (Exception e) {
					LOG.debug(e.toString());
				}
				
				List<ColumnFormat> columnFormats = new ArrayList<>();
				Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet, columnFormats);
				Set<String> keySet = headerIndex.keySet();
				int rowIndex = 1;
				Row row;
				Cell cell;
				Object val;
				
				for (Map task : taskDetails) {					
					row = sheet.getRow(rowIndex++);
					
					for (String key : keySet) {
						cell = row.getCell(headerIndex.get(key));
						
						if(cell == null) continue;
						
						val = task.get(key);
						
						switch(cell.getCellType()) {
						case Cell.CELL_TYPE_BLANK: {
							break;							
						}
						case Cell.CELL_TYPE_STRING: {
							cell.setCellValue(StringUtils.defaultString(String.valueOf(val), ""));
							break;
						}
						case Cell.CELL_TYPE_BOOLEAN: {
							cell.setCellValue((Boolean)val);							
							break;
						}
						case Cell.CELL_TYPE_NUMERIC: {
							if(HSSFDateUtil.isCellDateFormatted(cell)) {
								cell.setCellValue((Date)val);								
							} else {
								cell.setCellValue((Double)val);																						
							}
							break;
						}
						default: throw new Exception("Error on column: " + key);
						}
					}
				}
				
				workbook.write(out);
			} else {
				LOG.debug("Get byte");
				java.nio.file.Path path = Paths.get(filePath);
				data = Files.readAllBytes(path);					
			
				in = new ByteArrayInputStream(data);
				int bytes;
				
				while ((bytes = in.read()) != -1) {
					out.write(bytes);
				}
			}
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
		} finally {
			try { if(workbook != null) workbook.close(); } catch (Exception e2) {}
			try { if(fis != null) fis.close(); } catch (Exception e2) {}
			try { if(in != null) in.close(); } catch (Exception e2) {}			
			try { if(out != null) out.close(); } catch (Exception e2) {}			
		}	
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setIsCheckData(Boolean isCheckData) {
		this.isCheckData = isCheckData;
	}

	public void setTaskDetails(List<Map> taskDetails) {
		this.taskDetails = taskDetails;
	}

}
