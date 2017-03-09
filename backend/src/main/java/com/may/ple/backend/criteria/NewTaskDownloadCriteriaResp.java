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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;

public class NewTaskDownloadCriteriaResp extends CommonCriteriaResp implements StreamingOutput {
	private static final Logger LOG = Logger.getLogger(NewTaskDownloadCriteriaResp.class.getName());
	private String filePath;
	private Boolean isCheckData;
	private Boolean isByCriteria = false;
	private List<Map> taskDetails;
	
	@Override
	public void write(OutputStream os) throws IOException, WebApplicationException {
		OutputStream out = null;
		ByteArrayInputStream in = null;
		FileInputStream fis = null;
		XSSFWorkbook workbook = null;
		
		try {
			byte[] data;
			out = new BufferedOutputStream(os);
			
			if(isCheckData) {
				if(filePath.endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(new FileInputStream(filePath));
				} else {
					throw new CustomerException(5000, "Filetype not match");
				}
				
				if(!isByCriteria) workbook.setSheetName(0, workbook.getSheetName(0) + "_Validation");
				
				XSSFSheet sheet = workbook.getSheetAt(0);				
				List<ColumnFormat> columnFormats = new ArrayList<>();
				Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet, columnFormats);
				Set<String> keySet = headerIndex.keySet();
				CellCopyPolicy cellCopyPolicy = new CellCopyPolicy();
				cellCopyPolicy.setCopyCellStyle(true);
				Map<Integer, CellStyle> cellStyleMap = new HashMap<>();
				int rowIndex = 1;
				Row row;
				Cell cell;
				Object val;
				
				for (Map task : taskDetails) {			
					
					row = sheet.getRow(rowIndex++);
					
					if(rowIndex != 1) {
						sheet.copyRows(1, 1, rowIndex, cellCopyPolicy);	
					}
					
					if(row == null) {
						row = sheet.createRow(rowIndex - 1);
					}
					
					for (String key : keySet) {
						val = task.get(key);
						cell = row.getCell(headerIndex.get(key));
						
						if(val == null) {
							if(cell != null) {
								row.removeCell(cell);								
							}
							continue;
						}
						
						if(cell == null) {
							cell = row.createCell(headerIndex.get(key));
							cell.setCellStyle(cellStyleMap.get(headerIndex.get(key)));
						} else {
							if(cellStyleMap.get(headerIndex.get(key)) == null) {
								cellStyleMap.put(headerIndex.get(key), cell.getCellStyle());								
							}
						}
						
						if(val instanceof Date) {
							cell.setCellValue((Date)val);
						} else if(val instanceof Number) {
							cell.setCellValue((Double)val);
						} else if(val instanceof Boolean){
							cell.setCellValue((Boolean)val);
						} else {
							cell.setCellValue(StringUtils.defaultString(String.valueOf(val), ""));
						}
					}
				}
				
				if(isByCriteria) {
					int countNull = 0;
					
					while(true) {
						if(countNull == 10) break;
						
						row  = sheet.getRow(rowIndex++);
						
						if(row != null) {
							sheet.removeRow(row);
						} else {
							countNull++;
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

	public Boolean getIsByCriteria() {
		return isByCriteria;
	}

	public void setIsByCriteria(Boolean isByCriteria) {
		this.isByCriteria = isByCriteria;
	}

}
